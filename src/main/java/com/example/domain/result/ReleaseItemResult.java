package com.example.domain.result;

import com.example.domain.enums.ReleaseFailureReason;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReleaseItemResult {

    private final Long movieId;
    private final ResultType resultType;
    private final Double rating;
    private final ReleaseFailureReason failureReason;
    private final String message;

    public enum ResultType {
        SUCCESS, FAILED, SKIPPED
    }

    public static ReleaseItemResult success(Long movieId, Double rating) {
        return ReleaseItemResult.builder()
                .movieId(movieId)
                .resultType(ResultType.SUCCESS)
                .rating(rating)
                .message("Release completed successfully")
                .build();
    }

    public static ReleaseItemResult failed(Long movieId, ReleaseFailureReason reason, String message) {
        return ReleaseItemResult.builder()
                .movieId(movieId)
                .resultType(ResultType.FAILED)
                .failureReason(reason)
                .message(message)
                .build();
    }

    public static ReleaseItemResult skipped(Long movieId, String reason) {
        return ReleaseItemResult.builder()
                .movieId(movieId)
                .resultType(ResultType.SKIPPED)
                .message(reason)
                .build();
    }

    public boolean isSuccess() {
        return resultType == ResultType.SUCCESS;
    }

    public boolean isFailed() {
        return resultType == ResultType.FAILED;
    }

    public boolean isSkipped() {
        return resultType == ResultType.SKIPPED;
    }
}
