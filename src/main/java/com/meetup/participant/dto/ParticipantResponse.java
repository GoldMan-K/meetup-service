package com.meetup.participant.dto;

import com.meetup.participant.domain.MeetupParticipant;

import java.time.LocalDateTime;

public record ParticipantResponse(
        Long id,
        Long memberId,
        String role,
        String nickname,
        LocalDateTime joinedAt
) {
    public static ParticipantResponse from(MeetupParticipant p) {
        return from(p, null);
    }

    public static ParticipantResponse from(MeetupParticipant p, String nickname) {
        return new ParticipantResponse(
                p.getId(),
                p.getMemberId(),
                p.getRole(),
                nickname,
                p.getJoinedAt()
        );
    }
}
