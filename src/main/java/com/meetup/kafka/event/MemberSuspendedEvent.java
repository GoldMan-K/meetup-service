package com.meetup.kafka.event;

import java.time.Instant;

/**
 * 토픽: member.suspended
 * 발행자: member-service (타입 헤더 없이 발행)
 */
public record MemberSuspendedEvent(
        Long memberId,
        Instant occurredAt
) {
}

