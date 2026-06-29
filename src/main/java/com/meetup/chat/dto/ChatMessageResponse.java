package com.meetup.chat.dto;

import com.meetup.chat.domain.MeetupChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long meetupId,
        Long senderMemberId,
        String senderNickname,
        boolean isSystem,
        String message,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(MeetupChatMessage m) {
        return from(m, null);
    }

    public static ChatMessageResponse from(MeetupChatMessage m, String senderNickname) {
        return new ChatMessageResponse(
                m.getId(),
                m.getMeetupId(),
                m.getSenderMemberId(),
                senderNickname,
                m.isSystem(),
                m.getMessage(),
                m.getCreatedAt()
        );
    }
}
