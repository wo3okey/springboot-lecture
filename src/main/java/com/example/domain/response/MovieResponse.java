package com.example.domain.response;

import com.example.domain.entity.Actor;
import com.example.domain.entity.Movie;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MovieResponse {
    private final long id;

    private final String name;

    private final int productionYear;

    private final String directorName;

    private final List<String> actorNames;

    public static MovieResponse of(Movie entity) {
        return new MovieResponse(
                entity.getId(),
                entity.getName(),
                entity.getProductionYear(),
                entity.getDirector().getName(),
                entity.getActors().stream().map(Actor::getName).toList()
        );
    }
}
