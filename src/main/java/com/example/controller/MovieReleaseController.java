package com.example.controller;

import com.example.common.Response;
import com.example.domain.request.BatchReleaseRequest;
import com.example.domain.response.BatchReleaseResponse;
import com.example.domain.result.ReleaseItemResult;
import com.example.service.MovieReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movie Release", description = "영화 개봉 처리 API")
public class MovieReleaseController {

    private final MovieReleaseService movieReleaseService;

    @PostMapping("/release/batch")
    @Operation(summary = "영화 배치 개봉 처리", description = "여러 영화를 한번에 개봉 처리")
    public Response<BatchReleaseResponse> processBatchRelease(
            @Valid @RequestBody BatchReleaseRequest request
    ) {
        BatchReleaseResponse response = movieReleaseService.processBatchRelease(request);
        return Response.of(response);
    }

    @PostMapping("/{movieId}/release")
    @Operation(summary = "단일 영화 개봉 처리", description = "단일 영화 개봉")
    public Response<ReleaseItemResult> processRelease(
            @PathVariable Long movieId
    ) {
        BatchReleaseRequest request = new BatchReleaseRequest(List.of(movieId), null, null);
        BatchReleaseResponse response = movieReleaseService.processBatchRelease(request);

        if (!response.getSuccessItems().isEmpty()) {
            return Response.of(response.getSuccessItems().get(0));
        } else if (!response.getFailedItems().isEmpty()) {
            return Response.of(response.getFailedItems().get(0));
        } else {
            return Response.of(response.getSkippedItems().get(0));
        }
    }
}
