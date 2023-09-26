package com.example.domain.response;

import com.example.domain.entity.Movie;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MovieResponse {
    private final long id;

    private final String name;

    private final int productionYear;

    public static MovieResponse of(Movie entity) {
        return new MovieResponse(
                entity.getId(),
                entity.getName(),
                entity.getProductionYear()
        );
    }
}
