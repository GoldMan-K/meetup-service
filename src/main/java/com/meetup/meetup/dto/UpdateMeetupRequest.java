package com.meetup.meetup.dto;

import java.time.LocalDateTime;

public record UpdateMeetupRequest(
        String title,
        String typeCode,
        String regionCode,
        String place,
        String description,
        int capacity,
        LocalDateTime meetAt
) {}
