package com.meetup.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetupEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_MEETUP_JOINED   = "meetup.joined";
    private static final String TOPIC_MEETUP_CANCELED = "meetup.canceled";

    /**
     * 참가 신청 이벤트 발행 — 호스트 알림, 정원 도달 시 FULL 처리 트리거
     */
    public void publishMeetupJoined(Long meetupId, Long memberId, Long hostMemberId) {
        Map<String, Object> payload = Map.of(
                "meetupId", meetupId,
                "memberId", memberId,
                "hostMemberId", hostMemberId
        );
        kafkaTemplate.send(TOPIC_MEETUP_JOINED, String.valueOf(meetupId), payload);
        log.info("[Kafka] meetup.joined published: meetupId={}, memberId={}", meetupId, memberId);
    }

    /**
     * 모임 취소 이벤트 발행 — 참가자 전원에게 취소 알림 일괄 발송
     */
    public void publishMeetupCanceled(Long meetupId, Long hostMemberId) {
        Map<String, Object> payload = Map.of(
                "meetupId", meetupId,
                "hostMemberId", hostMemberId
        );
        kafkaTemplate.send(TOPIC_MEETUP_CANCELED, String.valueOf(meetupId), payload);
        log.info("[Kafka] meetup.canceled published: meetupId={}", meetupId);
    }
}
