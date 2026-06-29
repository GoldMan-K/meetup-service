package com.meetup.chat.dto;

public record TypingUserResponse(
        Long memberId,
        String nickname
) {}
