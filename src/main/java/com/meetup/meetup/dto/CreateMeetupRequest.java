package com.meetup.meetup.dto;

import java.time.LocalDateTime;

public record CreateMeetupRequest(
        String title,
        String typeCode,
        String regionCode,
        String place,
        String description,
        int capacity,
        LocalDateTime meetAt
) {}
