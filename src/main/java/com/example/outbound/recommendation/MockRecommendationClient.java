package com.example.outbound.recommendation;

import com.example.service.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MockRecommendationClient implements RecommendationClient {

    private static final Logger logger = LoggerFactory.getLogger(MockRecommendationClient.class);
    private final Random random = new Random();

    @Override
    public void indexMovie(Long movieId, String movieName, Double rating) {
        logger.info("[MockRecommendationClient] 영화 인덱싱 요청: movieId={}, movieName={}, rating={}",
                movieId, movieName, rating);

        if (random.nextInt(100) < 20) {
            logger.warn("[MockRecommendationClient] 인덱싱 실패 시뮬레이션: movieId={}", movieId);
            throw new ExternalServiceException("RecommendationEngine", "Failed to index movie");
        }

        simulateLatency();

        logger.info("[MockRecommendationClient] 영화 인덱싱 완료: movieId={}", movieId);
    }

    @Override
    public void removeMovie(Long movieId) {
        logger.info("[MockRecommendationClient] 영화 제거 요청: movieId={}", movieId);

        if (random.nextInt(100) < 10) {
            logger.warn("[MockRecommendationClient] 제거 실패 시뮬레이션: movieId={}", movieId);
            throw new ExternalServiceException("RecommendationEngine", "Failed to remove movie");
        }

        logger.info("[MockRecommendationClient] 영화 제거 완료: movieId={}", movieId);
    }

    private void simulateLatency() {
        try {
            Thread.sleep(50 + random.nextInt(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
