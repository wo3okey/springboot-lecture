package com.example.service;

import com.example.domain.entity.Movie;
import com.example.domain.entity.OutboxEvent;
import com.example.domain.enums.MovieStatus;
import com.example.domain.enums.ReleaseFailureReason;
import com.example.domain.event.MovieEventType;
import com.example.domain.event.MovieReleasedEvent;
import com.example.domain.request.BatchReleaseRequest;
import com.example.domain.response.BatchReleaseResponse;
import com.example.domain.result.ReleaseItemResult;
import com.example.outbound.notification.NotificationClient;
import com.example.outbound.rating.RatingClient;
import com.example.outbound.recommendation.RecommendationClient;
import com.example.repository.MovieRepository;
import com.example.repository.OutboxEventRepository;
import com.example.service.compensation.CompensationService;
import com.example.service.exception.CriticalException;
import com.example.service.support.RetryableOperation;
import com.example.service.validator.BatchReleaseValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MovieReleaseService {

    private static final Logger logger = LoggerFactory.getLogger(MovieReleaseService.class);
    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 500;
    private static final double DEFAULT_RATING = 0.0;

    private final MovieRepository movieRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final RatingClient ratingClient;
    private final RecommendationClient recommendationClient;
    private final NotificationClient notificationClient;
    private final BatchReleaseValidator validator;
    private final RetryableOperation retryableOperation;
    private final CompensationService compensationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public BatchReleaseResponse processBatchRelease(BatchReleaseRequest request) {
        logger.info("[Release] 배치 개봉 처리 시작: 요청 건수={}", request.getMovieIds().size());

        validator.validate(request);

        List<ReleaseItemResult> successList = new ArrayList<>();
        List<ReleaseItemResult> failedList = new ArrayList<>();
        List<ReleaseItemResult> skippedList = new ArrayList<>();

        for (Long movieId : request.getMovieIds()) {
            ReleaseItemResult result = processSingleMovie(movieId);

            switch (result.getResultType()) {
                case SUCCESS -> successList.add(result);
                case FAILED -> failedList.add(result);
                case SKIPPED -> skippedList.add(result);
            }
        }

        saveBatchCompletedEvent(successList, failedList, skippedList);

        logger.info("[Release] 배치 개봉 처리 완료: 성공={}, 실패={}, 스킵={}",
                successList.size(), failedList.size(), skippedList.size());

        return BatchReleaseResponse.of(successList, failedList, skippedList);
    }

    private ReleaseItemResult processSingleMovie(Long movieId) {
        logger.debug("[Release] 단일 영화 처리 시작: movieId={}", movieId);

        try {
            if (!validator.isValidMovieId(movieId)) {
                logger.warn("[Release] 유효하지 않은 Movie ID: {}", movieId);
                return ReleaseItemResult.failed(movieId, ReleaseFailureReason.INVALID_ID,
                        "Invalid movie ID: " + movieId);
            }

            Movie movie = movieRepository.findById(movieId).orElse(null);

            if (movie == null) {
                logger.warn("[Release] 영화를 찾을 수 없음: movieId={}", movieId);
                return ReleaseItemResult.failed(movieId, ReleaseFailureReason.NOT_FOUND,
                        "Movie not found: " + movieId);
            }

            return switch (movie.getStatus()) {
                case PRE_RELEASE -> executeReleaseProcess(movie);
                case RELEASED -> {
                    logger.info("[Release] 이미 개봉된 영화: movieId={}", movieId);
                    yield ReleaseItemResult.skipped(movieId, "Already released");
                }
                case END_OF_SHOWING -> {
                    logger.info("[Release] 상영 종료된 영화: movieId={}", movieId);
                    yield ReleaseItemResult.skipped(movieId, "Showing has ended");
                }
            };

        } catch (CriticalException e) {
            logger.error("[Release] 치명적 오류 발생: movieId={}", movieId, e);
            return ReleaseItemResult.failed(movieId, ReleaseFailureReason.CRITICAL_ERROR,
                    e.getMessage());
        } catch (Exception e) {
            logger.error("[Release] 예상치 못한 오류: movieId={}", movieId, e);
            return ReleaseItemResult.failed(movieId, ReleaseFailureReason.UNKNOWN_ERROR,
                    e.getMessage());
        }
    }

    private ReleaseItemResult executeReleaseProcess(Movie movie) {
        Long movieId = movie.getId();
        String movieName = movie.getName();

        logger.info("[Release] 개봉 프로세스 시작: movieId={}, movieName={}", movieId, movieName);

        try {
            Double rating = fetchRatingWithRetry(movieId, movieName);
            logger.info("[Release] 평점 조회 완료: movieId={}, rating={}", movieId, rating);

            indexToRecommendationEngine(movie, rating);

            sendNotificationOrSchedule(movie);

            movie.release(rating);
            logger.info("[Release] 영화 상태 업데이트 완료: movieId={}, status={}",
                    movieId, movie.getStatus());

            saveMovieReleasedEvent(movie, rating);

            logger.info("[Release] 개봉 처리 완료: movieId={}, name={}, rating={}",
                    movieId, movieName, rating);

            return ReleaseItemResult.success(movieId, rating);

        } catch (CriticalException e) {
            logger.error("[Release] 치명적 오류로 보상 로직 실행: movieId={}", movieId, e);
            compensationService.compensateRelease(movie);
            throw e;
        }
    }

    private Double fetchRatingWithRetry(Long movieId, String movieName) {
        return retryableOperation.executeWithRetry(
                MAX_RETRY,
                RETRY_DELAY_MS,
                () -> ratingClient.getRating(movieId, movieName).getRating(),
                () -> {
                    logger.warn("[Release] 평점 조회 실패, 기본값 사용: movieId={}, defaultRating={}",
                            movieId, DEFAULT_RATING);
                    return DEFAULT_RATING;
                },
                "RatingClient.getRating(" + movieId + ")"
        );
    }

    private void indexToRecommendationEngine(Movie movie, Double rating) {
        try {
            recommendationClient.indexMovie(movie.getId(), movie.getName(), rating);
            logger.info("[Release] 추천 엔진 인덱싱 완료: movieId={}", movie.getId());
        } catch (Exception e) {
            logger.warn("[Release] 추천 엔진 인덱싱 실패 (계속 진행): movieId={}, error={}",
                    movie.getId(), e.getMessage());
        }
    }

    private void sendNotificationOrSchedule(Movie movie) {
        try {
            notificationClient.sendReleaseNotification(movie.getId(), movie.getName());
            logger.info("[Release] 알림 발송 완료: movieId={}", movie.getId());
        } catch (Exception e) {
            logger.warn("[Release] 알림 발송 실패, Outbox에 저장: movieId={}", movie.getId());
            saveFailedNotificationEvent(movie);
        }
    }

    private void saveMovieReleasedEvent(Movie movie, Double rating) {
        try {
            MovieReleasedEvent event = MovieReleasedEvent.of(
                    movie.getId(),
                    movie.getName(),
                    rating,
                    movie.getReleasedAt()
            );

            OutboxEvent outboxEvent = OutboxEvent.of(
                    "Movie",
                    movie.getId().toString(),
                    MovieEventType.UPDATED.name(),
                    objectMapper.writeValueAsString(event)
            );

            outboxEventRepository.save(outboxEvent);
            logger.debug("[Release] 개봉 이벤트 저장 완료: movieId={}", movie.getId());

        } catch (JsonProcessingException e) {
            logger.error("[Release] 개봉 이벤트 JSON 변환 실패: movieId={}", movie.getId(), e);
        }
    }

    private void saveFailedNotificationEvent(Movie movie) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("movieId", movie.getId());
            payload.put("movieName", movie.getName());
            payload.put("eventType", "NOTIFICATION_FAILED");
            payload.put("retryRequired", true);

            OutboxEvent outboxEvent = OutboxEvent.of(
                    "Notification",
                    movie.getId().toString(),
                    MovieEventType.CREATED.name(),
                    objectMapper.writeValueAsString(payload)
            );

            outboxEventRepository.save(outboxEvent);
            logger.debug("[Release] 알림 실패 이벤트 저장 완료: movieId={}", movie.getId());

        } catch (JsonProcessingException e) {
            logger.error("[Release] 알림 실패 이벤트 JSON 변환 실패: movieId={}", movie.getId(), e);
        }
    }

    private void saveBatchCompletedEvent(
            List<ReleaseItemResult> successList,
            List<ReleaseItemResult> failedList,
            List<ReleaseItemResult> skippedList
    ) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("successCount", successList.size());
            payload.put("failedCount", failedList.size());
            payload.put("skippedCount", skippedList.size());
            payload.put("successMovieIds", successList.stream().map(ReleaseItemResult::getMovieId).toList());
            payload.put("failedMovieIds", failedList.stream().map(ReleaseItemResult::getMovieId).toList());
            payload.put("skippedMovieIds", skippedList.stream().map(ReleaseItemResult::getMovieId).toList());

            OutboxEvent outboxEvent = OutboxEvent.of(
                    "BatchRelease",
                    "batch-" + System.currentTimeMillis(),
                    MovieEventType.CREATED.name(),
                    objectMapper.writeValueAsString(payload)
            );

            outboxEventRepository.save(outboxEvent);
            logger.debug("[Release] 배치 완료 이벤트 저장 완료");

        } catch (JsonProcessingException e) {
            logger.error("[Release] 배치 완료 이벤트 JSON 변환 실패", e);
        }
    }
}
