package com.meetup.participant.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetup_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetupParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long meetupId;
    private Long memberId;
    private String role; // HOST | MEMBER
    private LocalDateTime joinedAt;

    @Builder
    public MeetupParticipant(Long meetupId, Long memberId, String role) {
        this.meetupId = meetupId;
        this.memberId = memberId;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isHost() {
        return "HOST".equals(this.role);
    }
}
