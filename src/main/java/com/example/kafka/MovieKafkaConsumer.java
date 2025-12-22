package com.example.kafka;

import com.example.domain.event.MovieEvent;
import com.example.service.LogService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer - 영화 이벤트 구독
 *
 * [동작 흐름]
 * Kafka Broker (movie-events 토픽)
 *       ↓
 * MovieKafkaConsumer.consumeMovieEvent()
 *       ↓
 * 비즈니스 로직 처리
 *       ↓
 * Acknowledgment.acknowledge() (수동 커밋)
 *
 * [핵심 개념]
 * - @KafkaListener: 지정된 토픽을 구독하는 Consumer 생성
 * - groupId: Consumer Group 식별자 (같은 그룹 내 Consumer는 파티션 분배)
 * - ConsumerRecord: 메시지 메타데이터 (토픽, 파티션, 오프셋 등) 포함
 * - Acknowledgment: 수동 커밋을 위한 인터페이스 (처리 완료 후 호출)
 */
@Component
@RequiredArgsConstructor
public class MovieKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MovieKafkaConsumer.class);

    private final LogService logService;

    /**
     * movie-events 토픽 구독 (수동 커밋 방식)
     *
     * @param record Kafka 메시지 (메타데이터 + 페이로드)
     * @param ack    수동 커밋용 Acknowledgment
     */
    @KafkaListener(
            topics = "movie-events",
            groupId = "movie-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMovieEvent(ConsumerRecord<String, MovieEvent> record, Acknowledgment ack) {
        MovieEvent event = record.value();

        logger.info("[Kafka Consumer] 이벤트 수신: topic={}, partition={}, offset={}, key={}, event={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                event);

        try {
            // 이벤트 타입에 따른 처리
            switch (event.getEventType()) {
                case CREATED:
                    handleMovieCreated(event);
                    break;
                case UPDATED:
                    handleMovieUpdated(event);
                    break;
                case DELETED:
                    handleMovieDeleted(event);
                    break;
                default:
                    logger.warn("[Kafka Consumer] 알 수 없는 이벤트 타입: {}", event.getEventType());
            }

            // 처리 성공 시 수동 커밋 (오프셋 저장)
            ack.acknowledge();
            logger.debug("[Kafka Consumer] 오프셋 커밋 완료: partition={}, offset={}",
                    record.partition(), record.offset());

        } catch (Exception e) {
            // 처리 실패 시 커밋하지 않음 → 재시작 시 다시 처리됨
            logger.error("[Kafka Consumer] 이벤트 처리 실패: partition={}, offset={}, error={}",
                    record.partition(), record.offset(), e.getMessage(), e);
            // 여기서 ack.acknowledge()를 호출하지 않으면 재처리됨
            // 필요시 DLT(Dead Letter Topic)로 전송하는 로직 추가 가능
            throw e;
        }
    }

    /**
     * 영화 생성 이벤트 처리
     *
     * [DLT 테스트]
     * movieName이 "DLT_TEST"인 경우 의도적으로 예외 발생
     * → 3회 재시도 후 DLT(movie-events.DLT)로 전송됨
     */
    private void handleMovieCreated(MovieEvent event) {
        logger.info("[Kafka Consumer] 영화 생성 이벤트 처리: movieId={}, movieName={}",
                event.getMovieId(), event.getMovieName());

        // DLT 테스트: movieName이 "DLT_TEST"면 예외 발생
        if ("DLT_TEST".equals(event.getMovieName())) {
            throw new RuntimeException("[DLT 테스트] 의도적 예외 발생 - DLT로 전송됩니다.");
        }

        // 로그 저장 (기존 동기 호출 → Kafka 통해 비동기 처리)
        logService.saveLog();
    }

    /**
     * 영화 수정 이벤트 처리
     */
    private void handleMovieUpdated(MovieEvent event) {
        logger.info("[Kafka Consumer] 영화 수정 이벤트 처리: movieId={}, movieName={}",
                event.getMovieId(), event.getMovieName());

        logService.saveLog();
    }

    /**
     * 영화 삭제 이벤트 처리
     */
    private void handleMovieDeleted(MovieEvent event) {
        logger.info("[Kafka Consumer] 영화 삭제 이벤트 처리: movieId={}",
                event.getMovieId());

        logService.saveLog();
    }
}
