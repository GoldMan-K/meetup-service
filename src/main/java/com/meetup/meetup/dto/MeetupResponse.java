package com.meetup.meetup.dto;

import com.meetup.meetup.domain.Meetup;

import java.time.LocalDateTime;

public record MeetupResponse(
        Long id,
        Long hostMemberId,
        String hostNickname,
        String title,
        String typeCode,
        String regionCode,
        String place,
        String description,
        int capacity,
        int participantCount,
        LocalDateTime meetAt,
        String status,
        LocalDateTime createdAt
) {
    public static MeetupResponse of(Meetup meetup, int participantCount) {
        return of(meetup, participantCount, null);
    }

    public static MeetupResponse of(Meetup meetup, int participantCount, String hostNickname) {
        return new MeetupResponse(
                meetup.getId(),
                meetup.getHostMemberId(),
                hostNickname,
                meetup.getTitle(),
                meetup.getTypeCode(),
                meetup.getRegionCode(),
                meetup.getPlace(),
                meetup.getDescription(),
                meetup.getCapacity(),
                participantCount,
                meetup.getMeetAt(),
                meetup.getStatus(),
                meetup.getCreatedAt()
        );
    }
}
