package com.meetup.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventConsumer {

    /**
     * member.suspended 이벤트 소비
     * - 정지된 회원이 호스트인 모임 처리 (선택 적용)
     * - 채팅 차단 등 추가 처리 가능
     */
    @KafkaListener(topics = "member.suspended", groupId = "meetup-service-group")
    public void handleMemberSuspended(Map<String, Object> payload) {
        try {
            Long memberId = Long.valueOf(payload.get("memberId").toString());
            log.info("[Kafka] member.suspended consumed: memberId={}", memberId);
            // 필요 시 정지 회원 처리 로직 추가
            // ex) 해당 회원이 호스트인 모임 상태 변경 등
        } catch (Exception e) {
            log.error("[Kafka] member.suspended 처리 실패: {}", e.getMessage(), e);
        }
    }
}
