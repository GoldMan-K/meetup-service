package com.meetup.meetup.service;

import com.meetup.global.exception.BusinessException;
import com.meetup.global.exception.ErrorCode;
import com.meetup.kafka.producer.MeetupEventProducer;
import com.meetup.meetup.domain.Meetup;
import com.meetup.meetup.dto.CreateMeetupRequest;
import com.meetup.meetup.dto.MeetupResponse;
import com.meetup.meetup.dto.UpdateMeetupRequest;
import com.meetup.meetup.repository.MeetupRepository;
import com.meetup.participant.domain.MeetupParticipant;
import com.meetup.participant.dto.ParticipantResponse;
import com.meetup.participant.repository.MeetupParticipantRepository;
import com.meetup.chat.domain.MeetupChatMessage;
import com.meetup.chat.repository.MeetupChatMessageRepository;
import com.meetup.member.service.MemberNicknameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetupService {

    private final MeetupRepository meetupRepository;
    private final MeetupParticipantRepository participantRepository;
    private final MeetupChatMessageRepository chatMessageRepository;
    private final MeetupEventProducer eventProducer;
    private final MemberNicknameService memberNicknameService;

    // ─── 목록 조회 ───────────────────────────────────────────────────────────

    public Page<MeetupResponse> getMeetupList(String regionCode, String typeCode,
                                              LocalDateTime meetAtFrom, LocalDateTime meetAtTo,
                                              Pageable pageable) {
        Page<Meetup> page = meetupRepository.findAllByFilter(regionCode, typeCode, meetAtFrom, meetAtTo, pageable);
        Map<Long, String> nicknamesByHost = memberNicknameService.resolveNicknames(
                page.getContent().stream().map(Meetup::getHostMemberId).toList()
        );

        return page.map(meetup -> MeetupResponse.of(
                meetup,
                participantRepository.countByMeetupId(meetup.getId()),
                resolveNickname(meetup.getHostMemberId(), nicknamesByHost)
        ));
    }

    // ─── 상세 조회 ───────────────────────────────────────────────────────────

    public MeetupResponse getMeetupDetail(Long meetupId) {
        Meetup meetup = findMeetup(meetupId);
        int count = participantRepository.countByMeetupId(meetupId);
        String hostNickname = resolveNickname(
                meetup.getHostMemberId(),
                memberNicknameService.resolveNicknames(List.of(meetup.getHostMemberId()))
        );
        return MeetupResponse.of(meetup, count, hostNickname);
    }

    public List<ParticipantResponse> getParticipants(Long meetupId) {
        findMeetup(meetupId); // 존재 여부 확인
        List<MeetupParticipant> participants = participantRepository.findAllByMeetupId(meetupId);
        Map<Long, String> nicknamesByMember = memberNicknameService.resolveNicknames(
                participants.stream().map(MeetupParticipant::getMemberId).toList()
        );

        return participants
                .stream()
                .map(participant -> ParticipantResponse.from(
                        participant,
                        resolveNickname(participant.getMemberId(), nicknamesByMember)
                ))
                .toList();
    }

    // ─── 생성 ────────────────────────────────────────────────────────────────

    @Transactional
    public MeetupResponse createMeetup(Long memberId, CreateMeetupRequest request) {
        Meetup meetup = Meetup.builder()
                .hostMemberId(memberId)
                .title(request.title())
                .typeCode(request.typeCode())
                .regionCode(request.regionCode())
                .place(request.place())
                .description(request.description())
                .capacity(request.capacity())
                .meetAt(request.meetAt())
                .build();
        meetupRepository.save(meetup);

        // 호스트도 participant로 삽입 (role=HOST)
        MeetupParticipant host = MeetupParticipant.builder()
                .meetupId(meetup.getId())
                .memberId(memberId)
                .role("HOST")
                .build();
        participantRepository.save(host);

        String hostNickname = resolveNickname(memberId, memberNicknameService.resolveNicknames(List.of(memberId)));
        return MeetupResponse.of(meetup, 1, hostNickname);
    }

    // ─── 수정 ────────────────────────────────────────────────────────────────

    @Transactional
    public MeetupResponse updateMeetup(Long memberId, Long meetupId, UpdateMeetupRequest request) {
        Meetup meetup = findMeetup(meetupId);
        meetup.validateHost(memberId);
        meetup.validateNotCanceled();

        meetup.update(request.title(), request.typeCode(), request.regionCode(),
                request.place(), request.description(), request.capacity(), request.meetAt());

        int count = participantRepository.countByMeetupId(meetupId);
        String hostNickname = resolveNickname(
                meetup.getHostMemberId(),
                memberNicknameService.resolveNicknames(List.of(meetup.getHostMemberId()))
        );
        return MeetupResponse.of(meetup, count, hostNickname);
    }

    // ─── 취소 ────────────────────────────────────────────────────────────────

    @Transactional
    public void cancelMeetup(Long memberId, Long meetupId) {
        Meetup meetup = findMeetup(meetupId);
        meetup.validateHost(memberId);
        meetup.validateNotCanceled();
        meetup.cancel();

        // meetup.canceled 이벤트 발행 → Notification Service에서 참가자 전원 알림
        eventProducer.publishMeetupCanceled(meetupId, memberId);
    }

    // ─── 모집 마감 ───────────────────────────────────────────────────────────

    @Transactional
    public void closeMeetup(Long memberId, Long meetupId) {
        Meetup meetup = findMeetup(meetupId);
        meetup.validateHost(memberId);
        meetup.validateNotCanceled();
        meetup.validateNotClosed();
        meetup.close();
    }

    // ─── 참가 신청 ───────────────────────────────────────────────────────────

    @Transactional
    public void joinMeetup(Long memberId, Long meetupId) {
        Meetup meetup = findMeetup(meetupId);
        meetup.validateNotCanceled();
        meetup.validateNotClosed();
        meetup.validateNotFull();

        if (participantRepository.existsByMeetupIdAndMemberId(meetupId, memberId)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        MeetupParticipant participant = MeetupParticipant.builder()
                .meetupId(meetupId)
                .memberId(memberId)
                .role("MEMBER")
                .build();
        participantRepository.save(participant);

        // 시스템 메시지 저장
        chatMessageRepository.save(
                MeetupChatMessage.systemMessage(meetupId, memberId + "님이 참가했습니다.")
        );

        // 정원 도달 시 FULL 전환
        int count = participantRepository.countByMeetupId(meetupId);
        if (count >= meetup.getCapacity()) {
            meetup.markFull();
        }

        // meetup.joined 이벤트 발행
        eventProducer.publishMeetupJoined(meetupId, memberId, meetup.getHostMemberId());
    }

    // ─── 참가 취소 ───────────────────────────────────────────────────────────

    @Transactional
    public void leaveMeetup(Long memberId, Long meetupId) {
        Meetup meetup = findMeetup(meetupId);
        meetup.validateNotCanceled();

        MeetupParticipant participant = participantRepository
                .findByMeetupIdAndMemberId(meetupId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_PARTICIPANT));

        if (participant.isHost()) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_LEAVE);
        }

        participantRepository.deleteByMeetupIdAndMemberId(meetupId, memberId);

        // 시스템 메시지
        chatMessageRepository.save(
                MeetupChatMessage.systemMessage(meetupId, memberId + "님이 나갔습니다.")
        );

        // FULL이었으면 OPEN으로 복구
        if ("FULL".equals(meetup.getStatus())) {
            meetup.markOpen();
        }
    }

    // ─── 공통 ────────────────────────────────────────────────────────────────

    private Meetup findMeetup(Long meetupId) {
        return meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));
    }

    private String resolveNickname(Long memberId, Map<Long, String> nicknamesByMember) {
        String nickname = nicknamesByMember.get(memberId);
        if (nickname == null || nickname.isBlank()) {
            return "회원 #" + memberId;
        }
        return nickname;
    }
}
