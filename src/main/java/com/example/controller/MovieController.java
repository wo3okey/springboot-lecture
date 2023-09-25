package com.example.controller;

import com.example.common.Response;
import com.example.domain.request.MovieRequest;
import com.example.domain.response.MovieResponse;
import com.example.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/api/v1/movies")
    public Response<List<MovieResponse>> getMovies(
            @RequestParam(value = "overYear", required = false) Integer overYear
    ) {
        return Response.of(movieService.getMovies(overYear));
    }

    @GetMapping("/api/v1/movies/{movieId}")
    public Response<MovieResponse> getMovie(
            @PathVariable(value = "movieId") long movieId
    ) {
        return Response.of(movieService.getMovie(movieId));
    }

    @PostMapping("/api/v1/movies")
    public void saveMovie(@RequestBody MovieRequest movieRequest) {
        movieService.saveMovie(movieRequest);
    }

    @PutMapping("/api/v1/movies/{movieId}")
    public void updateMovie(
            @PathVariable(value = "movieId") long movieId,
            @RequestBody MovieRequest movieRequest
    ) {
        movieService.updateMovie(movieId, movieRequest);
    }

    @DeleteMapping("/api/v1/movies/{movieId}")
    public void deleteMovie(@PathVariable(value = "movieId") long movieId) {
        movieService.removeMovie(movieId);
    }
}
