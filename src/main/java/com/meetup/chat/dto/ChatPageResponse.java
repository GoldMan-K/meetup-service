package com.meetup.chat.dto;

import java.util.List;

public record ChatPageResponse(
        List<ChatMessageResponse> messages,
        Long nextCursor,   // 다음 페이지 커서 (null이면 마지막 페이지)
        boolean hasNext
) {}
