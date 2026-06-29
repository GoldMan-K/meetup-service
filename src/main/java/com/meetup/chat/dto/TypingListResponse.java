package com.meetup.chat.dto;

import java.util.List;

public record TypingListResponse(
        Long meetupId,
        List<TypingUserResponse> typingUsers,
        int count
) {}
