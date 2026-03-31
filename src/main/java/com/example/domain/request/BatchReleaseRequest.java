package com.example.domain.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchReleaseRequest {

    @NotNull(message = "Movie IDs cannot be null")
    @Size(min = 1, max = 100, message = "Movie IDs must be between 1 and 100")
    private List<Long> movieIds;

    private String requestedBy;

    private String reason;
}
