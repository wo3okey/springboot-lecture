package com.example.service.validator;

import com.example.domain.request.MovieRequest;
import org.springframework.stereotype.Component;

/**
 * 영화 관련 입력 검증을 담당하는 Validator
 */
@Component
public class MovieValidator {

    /**
     * 영화 ID 유효성 검증
     *
     * @param movieId 검증할 영화 ID
     * @throws IllegalArgumentException movieId가 null이거나 0 이하인 경우
     */
    public void validateMovieId(Long movieId) {
        if (movieId == null) {
            throw new IllegalArgumentException("Movie ID cannot be null");
        }
        if (movieId <= 0) {
            throw new IllegalArgumentException("Movie ID must be greater than 0");
        }
    }

    /**
     * 영화 요청 DTO 유효성 검증
     *
     * @param movieRequest 검증할 영화 요청 DTO
     * @throws IllegalArgumentException movieRequest가 null이거나 필수 필드가 누락된 경우
     */
    public void validateMovieRequest(MovieRequest movieRequest) {
        if (movieRequest == null) {
            throw new IllegalArgumentException("Movie request cannot be null");
        }
        if (movieRequest.getName() == null || movieRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Movie name cannot be null or empty");
        }
        if (movieRequest.getProductionYear() < 1900 || movieRequest.getProductionYear() > 2100) {
            throw new IllegalArgumentException("Production year must be between 1900 and 2100");
        }
    }
}
