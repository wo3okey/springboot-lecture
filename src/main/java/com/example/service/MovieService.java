package com.example.service;

import com.example.domain.entity.Movie;
import com.example.domain.request.MovieRequest;
import com.example.domain.response.MovieResponse;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {
    private final List<Movie> movies = new ArrayList<>();

    @PostConstruct
    public void init() {
        movies.addAll(
                List.of(
                        new Movie(1L, "기생충", 2020),
                        new Movie(2L, "오펜하이머", 2023),
                        new Movie(3L, "코딩의 추억", 1998),
                        new Movie(4L, "짱구는 못말려 코딩의 후예", 2004),
                        new Movie(5L, "코딩도시4", 2022)
                )
        );
    }

    public MovieResponse getMovie(long movieId) {
        Movie movie = movies.stream().filter(m -> m.getId() == movieId).findFirst().orElseThrow();
        return MovieResponse.of(movie);
    }

    public List<MovieResponse> getMovies(Integer overYear) {
        if (overYear != null) {
            return movies.stream().filter(m -> overYear < m.getProductionYear()).map(MovieResponse::of).toList();
        }
        return movies.stream().map(MovieResponse::of).toList();
    }

    public void saveMovie(MovieRequest movieRequest) {
        long newIndex = movies.size() + 1;
        Movie movie = new Movie(newIndex, movieRequest.getName(), movieRequest.getProductionYear());
        movies.add(movie);
    }

    public void updateMovie(long movieId, MovieRequest movieRequest) {
        Movie movie = movies.stream().filter(m -> m.getId() == movieId).findFirst().orElseThrow();
        movie.setName(movieRequest.getName());
        movie.setProductionYear(movieRequest.getProductionYear());
    }

    public void removeMovie(long movieId) {
        Movie movie = movies.stream().filter(m -> m.getId() == movieId).findFirst().orElseThrow();
        movies.remove(movie);
    }
}
