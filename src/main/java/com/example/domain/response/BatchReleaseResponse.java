package com.example.domain.response;

import com.example.domain.result.ReleaseItemResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BatchReleaseResponse {

    private final int totalCount;
    private final int successCount;
    private final int failedCount;
    private final int skippedCount;
    private final List<ReleaseItemResult> successItems;
    private final List<ReleaseItemResult> failedItems;
    private final List<ReleaseItemResult> skippedItems;
    private final LocalDateTime processedAt;

    public static BatchReleaseResponse of(
            List<ReleaseItemResult> success,
            List<ReleaseItemResult> failed,
            List<ReleaseItemResult> skipped
    ) {
        return BatchReleaseResponse.builder()
                .totalCount(success.size() + failed.size() + skipped.size())
                .successCount(success.size())
                .failedCount(failed.size())
                .skippedCount(skipped.size())
                .successItems(success)
                .failedItems(failed)
                .skippedItems(skipped)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
