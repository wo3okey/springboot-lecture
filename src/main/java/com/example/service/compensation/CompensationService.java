package com.example.service.compensation;

import com.example.domain.entity.Movie;
import com.example.domain.entity.OutboxEvent;
import com.example.domain.enums.MovieStatus;
import com.example.domain.event.MovieEventType;
import com.example.outbound.notification.NotificationClient;
import com.example.outbound.recommendation.RecommendationClient;
import com.example.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompensationService {

    private static final Logger logger = LoggerFactory.getLogger(CompensationService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationClient notificationClient;
    private final RecommendationClient recommendationClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public void compensateRelease(Movie movie) {
        logger.info("[Compensation] 보상 로직 시작: movieId={}", movie.getId());

        try {
            if (movie.getStatus() == MovieStatus.RELEASED) {
                movie.cancelRelease();
                logger.info("[Compensation] 영화 상태 롤백 완료: movieId={}", movie.getId());
            }

            try {
                recommendationClient.removeMovie(movie.getId());
                logger.info("[Compensation] 추천 엔진에서 영화 제거 완료: movieId={}", movie.getId());
            } catch (Exception e) {
                logger.warn("[Compensation] 추천 엔진 제거 실패 (무시): movieId={}, error={}",
                        movie.getId(), e.getMessage());
            }

            try {
                notificationClient.sendReleaseFailedNotification(movie.getId(), movie.getName());
                logger.info("[Compensation] 실패 알림 발송 완료: movieId={}", movie.getId());
            } catch (Exception e) {
                logger.warn("[Compensation] 실패 알림 발송 실패 (무시): movieId={}, error={}",
                        movie.getId(), e.getMessage());
            }

            saveCompensationEvent(movie);

            logger.info("[Compensation] 보상 로직 완료: movieId={}", movie.getId());

        } catch (Exception e) {
            logger.error("[Compensation] 보상 로직 실패: movieId={}", movie.getId(), e);
            throw new RuntimeException("Compensation failed for movie: " + movie.getId(), e);
        }
    }

    private void saveCompensationEvent(Movie movie) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("movieId", movie.getId());
            payload.put("movieName", movie.getName());
            payload.put("compensationReason", "Release process failed");
            payload.put("previousStatus", MovieStatus.PRE_RELEASE.name());

            OutboxEvent event = OutboxEvent.of(
                    "Movie",
                    movie.getId().toString(),
                    MovieEventType.DELETED.name(),
                    objectMapper.writeValueAsString(payload)
            );

            outboxEventRepository.save(event);
            logger.info("[Compensation] 보상 이벤트 저장 완료: movieId={}", movie.getId());

        } catch (JsonProcessingException e) {
            logger.error("[Compensation] 보상 이벤트 JSON 변환 실패: movieId={}", movie.getId(), e);
        }
    }
}
