package com.example.kafka;

import com.example.domain.entity.OutboxEvent;
import com.example.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 이벤트 Publisher
 *
 * [역할]
 * - Outbox 테이블을 주기적으로 폴링
 * - 미발행 이벤트를 Kafka로 발행
 * - 발행 완료 후 상태 업데이트
 *
 * [Polling 방식의 장단점]
 * 장점:
 * - 구현이 단순
 * - 별도 인프라 불필요
 *
 * 단점:
 * - 폴링 주기만큼 지연 발생
 * - DB 부하 (개선: CDC 방식 사용)
 */
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private static final int BATCH_SIZE = 100;
    private static final String TOPIC_PREFIX = "";  // 토픽명 = aggregateType.toLowerCase() + "-events"

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 미발행 이벤트 폴링 및 발행
     * 1초마다 실행
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> unpublishedEvents = outboxEventRepository.findUnpublishedEvents(BATCH_SIZE);

        if (unpublishedEvents.isEmpty()) {
            return;
        }

        logger.info("[Outbox Publisher] 미발행 이벤트 {} 건 처리 시작", unpublishedEvents.size());

        for (OutboxEvent event : unpublishedEvents) {
            try {
                publishToKafka(event);
                event.markAsPublished();
                logger.debug("[Outbox Publisher] 이벤트 발행 완료: id={}, type={}, aggregateId={}",
                        event.getId(), event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                logger.error("[Outbox Publisher] 이벤트 발행 실패: id={}, error={}",
                        event.getId(), e.getMessage(), e);
                // 실패한 이벤트는 다음 폴링에서 재시도
            }
        }
    }

    /**
     * Kafka로 이벤트 발행
     */
    private void publishToKafka(OutboxEvent event) {
        String topic = resolveTopicName(event.getAggregateType());
        String key = event.getAggregateId();
        String value = event.getPayload();

        kafkaTemplate.send(topic, key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.error("[Outbox Publisher] Kafka 전송 실패: topic={}, key={}, error={}",
                                topic, key, ex.getMessage());
                    } else {
                        logger.info("[Outbox Publisher] Kafka 전송 성공: topic={}, partition={}, offset={}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * 토픽 이름 결정
     * 예: "Movie" → "movie-events"
     */
    private String resolveTopicName(String aggregateType) {
        return aggregateType.toLowerCase() + "-events";
    }

    /**
     * 오래된 발행 완료 이벤트 정리
     * 매일 자정에 실행 (7일 이전 데이터 삭제)
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        int deletedCount = outboxEventRepository.deletePublishedEventsBefore(threshold);
        logger.info("[Outbox Cleanup] {} 건의 오래된 이벤트 삭제 완료", deletedCount);
    }
}
