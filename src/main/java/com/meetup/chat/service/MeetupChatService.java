package com.meetup.chat.service;

import com.meetup.chat.domain.MeetupChatMessage;
import com.meetup.chat.dto.ChatMessageRequest;
import com.meetup.chat.dto.ChatMessageResponse;
import com.meetup.chat.dto.ChatPageResponse;
import com.meetup.chat.repository.MeetupChatMessageRepository;
import com.meetup.global.exception.BusinessException;
import com.meetup.global.exception.ErrorCode;
import com.meetup.participant.repository.MeetupParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetupChatService {

    private final MeetupChatMessageRepository chatMessageRepository;
    private final MeetupParticipantRepository participantRepository;

    private static final int DEFAULT_PAGE_SIZE = 30;

    // ─── 채팅 조회 (커서 페이징) ─────────────────────────────────────────────

    public ChatPageResponse getChatMessages(Long meetupId, Long cursor, int size) {
        int fetchSize = Math.min(size, DEFAULT_PAGE_SIZE);
        List<MeetupChatMessage> messages = chatMessageRepository
                .findByMeetupIdWithCursor(meetupId, cursor, PageRequest.of(0, fetchSize + 1));

        boolean hasNext = messages.size() > fetchSize;
        List<MeetupChatMessage> result = hasNext ? messages.subList(0, fetchSize) : messages;

        Long nextCursor = hasNext ? result.get(result.size() - 1).getId() : null;

        return new ChatPageResponse(
                result.stream().map(ChatMessageResponse::from).toList(),
                nextCursor,
                hasNext
        );
    }

    // ─── 채팅 전송 ───────────────────────────────────────────────────────────

    @Transactional
    public ChatMessageResponse sendMessage(Long memberId, Long meetupId, ChatMessageRequest request) {
        // 참가자만 채팅 가능
        if (!participantRepository.existsByMeetupIdAndMemberId(meetupId, memberId)) {
            throw new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT);
        }

        MeetupChatMessage message = MeetupChatMessage.builder()
                .meetupId(meetupId)
                .senderMemberId(memberId)
                .isSystem(false)
                .message(request.message())
                .build();

        return ChatMessageResponse.from(chatMessageRepository.save(message));
    }
}
