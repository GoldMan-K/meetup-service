package com.meetup.kafka.consumer;

import com.meetup.kafka.event.MemberSuspendedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventConsumer {

    /**
     * member.suspended 이벤트 소비
     * - member-service 는 타입 헤더 없이 발행하므로 containerFactory 를 명시해야 함
     * - 정지된 회원이 호스트인 모임 처리 (선택 적용)
     */
    @KafkaListener(
            topics = "member.suspended",
            groupId = "meetup-service-group",
            containerFactory = "memberSuspendedListenerFactory"
    )
    public void handleMemberSuspended(MemberSuspendedEvent event) {
        try {
            log.info("[Kafka] member.suspended consumed: memberId={}", event.memberId());
            // 필요 시 정지 회원 처리 로직 추가
            // ex) 해당 회원이 호스트인 모임 상태 변경 등
        } catch (Exception e) {
            log.error("[Kafka] member.suspended 처리 실패: {}", e.getMessage(), e);
        }
    }
}
