package com.example.outbound.rating;

import com.example.domain.response.RatingResponse;
import com.example.service.exception.CriticalException;
import com.example.service.exception.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MockRatingClient implements RatingClient {

    private static final Logger logger = LoggerFactory.getLogger(MockRatingClient.class);
    private final Random random = new Random();

    @Override
    public RatingResponse getRating(Long movieId, String movieName) {
        logger.info("[MockRatingClient] 평점 조회 요청: movieId={}, movieName={}", movieId, movieName);

        int chance = random.nextInt(100);

        if (chance < 30) {
            logger.warn("[MockRatingClient] 평점 조회 실패 시뮬레이션 (재시도 가능): movieId={}", movieId);
            throw new RetryableException("Rating service temporarily unavailable");
        }

        if (chance < 40) {
            logger.error("[MockRatingClient] 평점 조회 치명적 오류 시뮬레이션: movieId={}", movieId);
            throw new CriticalException("RATING_CRITICAL", "Rating service critical error");
        }

        double rating = 3.0 + random.nextDouble() * 2.0;
        double roundedRating = Math.round(rating * 10.0) / 10.0;

        logger.info("[MockRatingClient] 평점 조회 성공: movieId={}, rating={}", movieId, roundedRating);
        return new RatingResponse(movieId, roundedRating);
    }
}
