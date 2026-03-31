package com.example.service.validator;

import com.example.domain.request.BatchReleaseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BatchReleaseValidator {

    private static final Logger logger = LoggerFactory.getLogger(BatchReleaseValidator.class);
    private static final int MAX_BATCH_SIZE = 100;

    public void validate(BatchReleaseRequest request) {
        logger.debug("[Validator] 배치 개봉 요청 검증 시작");

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getMovieIds() == null) {
            throw new IllegalArgumentException("Movie IDs cannot be null");
        }

        if (request.getMovieIds().isEmpty()) {
            throw new IllegalArgumentException("Movie IDs cannot be empty");
        }

        if (request.getMovieIds().size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Batch size cannot exceed %d. Current size: %d",
                            MAX_BATCH_SIZE, request.getMovieIds().size()));
        }

        long distinctCount = request.getMovieIds().stream().distinct().count();
        if (distinctCount != request.getMovieIds().size()) {
            logger.warn("[Validator] 중복된 Movie ID가 포함되어 있습니다. 원본: {}, 고유: {}",
                    request.getMovieIds().size(), distinctCount);
        }

        logger.debug("[Validator] 배치 개봉 요청 검증 완료: 요청 건수={}", request.getMovieIds().size());
    }

    public boolean isValidMovieId(Long movieId) {
        return movieId != null && movieId > 0;
    }
}
