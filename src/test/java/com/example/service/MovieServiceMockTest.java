package com.example.service;

import com.example.domain.entity.Actor;
import com.example.domain.entity.Director;
import com.example.domain.entity.Movie;
import com.example.domain.request.MovieRequest;
import com.example.domain.response.MovieResponse;
import com.example.repository.MovieRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceMockTest {
    @Mock
    private MovieRepository movieRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private MovieService movieService;

    @Test
    public void 영화단건조회_정상조회_테스트() {
        // given
        int movieId = 1;
        Movie movie = new Movie();
        movie.setName("하이");
        movie.setDirector(new Director());
        movie.setActors(List.of(new Actor()));

        // when
        when(movieRepository.findById(anyLong())).thenReturn(Optional.of(movie));

        // then
        MovieResponse movieResponse = movieService.getMovie(movieId);
        assertNotNull(movieResponse);
    }

    @Test
    public void 영화단건조회_불가_테스트() {
        // given
        int movieId = 1;
        Movie movie = new Movie();
        movie.setName("하이");
        movie.setDirector(new Director());
        movie.setActors(List.of(new Actor()));

        // when
        when(movieRepository.findById(anyLong())).thenReturn(null);

        // then
        assertThrows(NullPointerException.class, () -> movieService.getMovie(movieId));
    }

    @Test
    public void 영화단건_저장_테스트() {
        // given
        MovieRequest request = new MovieRequest("영화명", 2002, 1L);
        Movie movie = new Movie("영화명", 2002);

        // when
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);
        doNothing().when(logService).saveLog();

        // then
        movieService.saveMovie(request);
    }
}
