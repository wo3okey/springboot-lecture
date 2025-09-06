package com.example.service;

import com.example.domain.entity.Movie;
import com.example.domain.request.MovieRequest;
import com.example.domain.response.MovieResponse;
import com.example.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final LogService logService;

    @Transactional(readOnly = true)
    public MovieResponse getMovie(long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        return MovieResponse.of(movie);
    }

    @Transactional(readOnly = true)
    public Movie getMovieEntity(long movieId) {
        return movieRepository.findById(movieId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getMovies() {
        List<Movie> movies = movieRepository.findAll();

        return movies.stream().map(MovieResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesMultiFetchError() {
        List<Movie> movies = movieRepository.findAllMultiFetchError();

        return movies.stream().map(MovieResponse::of).toList();
    }

    @Transactional
    public void saveMovie(MovieRequest movieRequest) {
        Movie movie1 = new Movie(movieRequest.getName(), movieRequest.getProductionYear());
        movieRepository.save(movie1);
        logService.saveLog();
    }

    @Transactional
    public void updateMovie(long movieId, MovieRequest movieRequest) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        movie.updateName(movieRequest.getName());
        movie.updateName("변경2");
        movie.updateName("변경3");
    }

    @Transactional
    public void removeMovie(long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        movieRepository.delete(movie);
    }
}
