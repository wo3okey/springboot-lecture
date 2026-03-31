package com.example.controller;

import com.example.common.Response;
import com.example.domain.entity.Movie;
import com.example.domain.request.MovieRequest;
import com.example.domain.response.MovieResponse;
import com.example.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;
    private final Logger logger = LoggerFactory.getLogger(MovieController.class);

    @GetMapping("/api/v1/movies")
    public Response<List<MovieResponse>> getMovies() {
        return Response.of(movieService.getAllMovies());
    }

    @GetMapping("/api/v1/movies/multi-fetch-error")
    public Response<List<MovieResponse>> getMoviesMultiFetchError() {
        return Response.of(movieService.getAllMoviesWithMultiFetchError());
    }

    @GetMapping("/api/v1/movies/{movieId}")
    public Response<MovieResponse> getMovie(
            @PathVariable(value = "movieId") long movieId
    ) {
        return Response.of(movieService.getMovieById(movieId));
    }

    // required - spring.jpa.open-in-view: true
    @GetMapping("/api/v1/movies/{movieId}/osiv-error")
    public Response<MovieResponse> getMovieEntity(
            @PathVariable(value = "movieId") long movieId
    ) {
        Movie movie = movieService.getMovieEntityById(movieId);
        logger.info("Director: {}", movie.getDirector());
        logger.info("Actors: {}", movie.getActors());
        return Response.of(MovieResponse.of(movie));
    }

    @PostMapping("/api/v1/movies")
    public void saveMovie(@Valid @RequestBody MovieRequest movieRequest) {
        movieService.saveMovie(movieRequest);
    }

    @PutMapping("/api/v1/movies/{movieId}")
    public void updateMovie(
            @PathVariable(value = "movieId") long movieId,
            @Valid @RequestBody MovieRequest movieRequest
    ) {
        movieService.updateMovie(movieId, movieRequest);
    }

    @DeleteMapping("/api/v1/movies/{movieId}")
    public Response<Void> deleteMovie(@PathVariable(value = "movieId") long movieId) {
        movieService.deleteMovie(movieId);
        return Response.success();
    }
}
