package com.meetup.chat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetup_chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetupChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long meetupId;
    private Long senderMemberId; // 시스템 메시지는 null
    private boolean isSystem;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;

    @Builder
    public MeetupChatMessage(Long meetupId, Long senderMemberId, boolean isSystem, String message) {
        this.meetupId = meetupId;
        this.senderMemberId = senderMemberId;
        this.isSystem = isSystem;
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static MeetupChatMessage systemMessage(Long meetupId, String message) {
        return MeetupChatMessage.builder()
                .meetupId(meetupId)
                .senderMemberId(null)
                .isSystem(true)
                .message(message)
                .build();
    }
}
