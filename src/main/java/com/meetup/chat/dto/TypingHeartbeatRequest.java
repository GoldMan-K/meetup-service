package com.meetup.chat.dto;

public record TypingHeartbeatRequest(
        Boolean isTyping,
        Integer ttlSeconds
) {}
