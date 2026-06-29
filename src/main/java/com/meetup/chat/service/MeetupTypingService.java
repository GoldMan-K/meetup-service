package com.meetup.chat.service;

import com.meetup.chat.dto.TypingHeartbeatRequest;
import com.meetup.chat.dto.TypingListResponse;
import com.meetup.chat.dto.TypingUserResponse;
import com.meetup.global.exception.BusinessException;
import com.meetup.global.exception.ErrorCode;
import com.meetup.meetup.domain.Meetup;
import com.meetup.meetup.repository.MeetupRepository;
import com.meetup.member.service.MemberNicknameService;
import com.meetup.participant.repository.MeetupParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetupTypingService {

    private static final int MIN_TTL_SECONDS = 1;
    private static final int MAX_TTL_SECONDS = 30;

    private final StringRedisTemplate redisTemplate;
    private final MeetupRepository meetupRepository;
    private final MeetupParticipantRepository participantRepository;
    private final MemberNicknameService memberNicknameService;

    @Value("${typing.ttl-seconds:5}")
    private int defaultTtlSeconds;

    @Transactional
    public void heartbeat(Long meetupId, Long memberId, TypingHeartbeatRequest request) {
        if (request == null || request.isTyping() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        validateTypingAllowed(meetupId, memberId);

        if (Boolean.TRUE.equals(request.isTyping())) {
            int ttlSeconds = resolveTtlSeconds(request.ttlSeconds());
            String memberKey = memberTypingKey(meetupId, memberId);

            redisTemplate.opsForValue().set(memberKey, "1", Duration.ofSeconds(ttlSeconds));
            redisTemplate.opsForSet().add(typingIndexKey(meetupId), String.valueOf(memberId));
            redisTemplate.expire(typingIndexKey(meetupId), Duration.ofMinutes(30));
            return;
        }

        clearTyping(meetupId, memberId);
    }

    public TypingListResponse getTypingUsers(Long meetupId, Long requesterMemberId) {
        validateTypingAllowed(meetupId, requesterMemberId);

        String indexKey = typingIndexKey(meetupId);
        Set<String> members = redisTemplate.opsForSet().members(indexKey);
        if (members == null || members.isEmpty()) {
            return new TypingListResponse(meetupId, List.of(), 0);
        }

        List<Long> activeMemberIds = new ArrayList<>();
        for (String rawMemberId : members) {
            Long memberId = parseMemberId(rawMemberId);
            if (memberId == null) {
                redisTemplate.opsForSet().remove(indexKey, rawMemberId);
                continue;
            }

            Boolean exists = redisTemplate.hasKey(memberTypingKey(meetupId, memberId));
            if (!Boolean.TRUE.equals(exists)) {
                redisTemplate.opsForSet().remove(indexKey, rawMemberId);
                continue;
            }

            if (!memberId.equals(requesterMemberId)) {
                activeMemberIds.add(memberId);
            }
        }

        if (activeMemberIds.isEmpty()) {
            return new TypingListResponse(meetupId, List.of(), 0);
        }

        Map<Long, String> nicknamesByMember = memberNicknameService.resolveNicknames(activeMemberIds);
        List<TypingUserResponse> typingUsers = activeMemberIds.stream()
                .distinct()
                .map(memberId -> new TypingUserResponse(memberId, resolveNickname(memberId, nicknamesByMember)))
                .toList();

        return new TypingListResponse(meetupId, typingUsers, typingUsers.size());
    }

    private void validateTypingAllowed(Long meetupId, Long memberId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (meetup.isCanceled() || "CLOSED".equals(meetup.getStatus()) || "FULL".equals(meetup.getStatus())) {
            throw new BusinessException(ErrorCode.TYPING_DISABLED);
        }

        if (!participantRepository.existsByMeetupIdAndMemberId(meetupId, memberId)) {
            throw new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT);
        }
    }

    private int resolveTtlSeconds(Integer requestedTtlSeconds) {
        int ttl = requestedTtlSeconds == null ? defaultTtlSeconds : requestedTtlSeconds;
        if (ttl < MIN_TTL_SECONDS || ttl > MAX_TTL_SECONDS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return ttl;
    }

    private void clearTyping(Long meetupId, Long memberId) {
        redisTemplate.delete(memberTypingKey(meetupId, memberId));
        redisTemplate.opsForSet().remove(typingIndexKey(meetupId), String.valueOf(memberId));
    }

    private Long parseMemberId(String rawMemberId) {
        try {
            return Long.parseLong(rawMemberId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveNickname(Long memberId, Map<Long, String> nicknamesByMember) {
        String nickname = nicknamesByMember.get(memberId);
        if (nickname == null || nickname.isBlank()) {
            return "회원 #" + memberId;
        }
        return nickname;
    }

    private String typingIndexKey(Long meetupId) {
        return "meetup:typing:index:" + meetupId;
    }

    private String memberTypingKey(Long meetupId, Long memberId) {
        return "meetup:typing:member:" + meetupId + ":" + memberId;
    }
}
