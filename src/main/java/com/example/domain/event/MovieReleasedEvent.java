package com.example.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MovieReleasedEvent {

    private final Long movieId;
    private final String movieName;
    private final Double rating;
    private final LocalDateTime releasedAt;
    private final LocalDateTime occurredAt;

    public static MovieReleasedEvent of(Long movieId, String movieName, Double rating, LocalDateTime releasedAt) {
        return MovieReleasedEvent.builder()
                .movieId(movieId)
                .movieName(movieName)
                .rating(rating)
                .releasedAt(releasedAt)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
