package com.example.kafka;

import com.example.domain.event.MovieEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer - 영화 이벤트 발행
 *
 * [동작 흐름]
 * MovieService.saveMovie()
 *       ↓
 * MovieKafkaProducer.publishMovieCreated()
 *       ↓
 * KafkaTemplate.send()
 *       ↓
 * Kafka Broker (movie-events 토픽)
 */
@Component
@RequiredArgsConstructor
public class MovieKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(MovieKafkaProducer.class);
    private static final String TOPIC = "movie-events";

    private final KafkaTemplate<String, MovieEvent> kafkaTemplate;

    /**
     * 영화 생성 이벤트 발행
     *
     * @param movieId   영화 ID
     * @param movieName 영화명
     */
    public void publishMovieCreated(Long movieId, String movieName) {
        MovieEvent event = MovieEvent.created(movieId, movieName);
        publish(event);
    }

    /**
     * 영화 수정 이벤트 발행
     *
     * @param movieId   영화 ID
     * @param movieName 영화명
     */
    public void publishMovieUpdated(Long movieId, String movieName) {
        MovieEvent event = MovieEvent.updated(movieId, movieName);
        publish(event);
    }

    /**
     * 영화 삭제 이벤트 발행
     *
     * @param movieId 영화 ID
     */
    public void publishMovieDeleted(Long movieId) {
        MovieEvent event = MovieEvent.deleted(movieId);
        publish(event);
    }

    /**
     * 이벤트 발행 (내부 메서드)
     * - Key: movieId (같은 영화의 이벤트는 같은 파티션으로 → 순서 보장)
     * - Value: MovieEvent (JSON 직렬화)
     */
    private void publish(MovieEvent event) {
        String key = String.valueOf(event.getMovieId());

        CompletableFuture<SendResult<String, MovieEvent>> future =
                kafkaTemplate.send(TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // 성공
                logger.info("[Kafka Producer] 이벤트 발행 성공: topic={}, partition={}, offset={}, event={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event);
            } else {
                // 실패
                logger.error("[Kafka Producer] 이벤트 발행 실패: event={}", event, ex);
            }
        });
    }
}
