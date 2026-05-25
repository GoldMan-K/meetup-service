package com.meetup.participant.dto;

import com.meetup.participant.domain.MeetupParticipant;

import java.time.LocalDateTime;

public record ParticipantResponse(
        Long id,
        Long memberId,
        String role,
        LocalDateTime joinedAt
) {
    public static ParticipantResponse from(MeetupParticipant p) {
        return new ParticipantResponse(
                p.getId(),
                p.getMemberId(),
                p.getRole(),
                p.getJoinedAt()
        );
    }
}
