package com.example.service;

import com.example.domain.entity.Movie;
import com.example.domain.request.MovieRequest;
import com.example.kafka.MovieKafkaProducer;
import com.example.repository.MovieRepository;
import com.example.repository.OutboxEventRepository;
import com.example.service.exception.MovieNotFoundException;
import com.example.service.validator.MovieValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {
    @Mock
    private MovieRepository movieRepository;

    @Mock
    private LogService logService;

    @Mock
    private MovieValidator movieValidator;

    @Mock
    private MovieKafkaProducer movieKafkaProducer;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MovieService movieService;

    @Test
    public void 영화단건조회_불가_테스트() {
        // given
        Long movieId = 1L;

        // when
        doNothing().when(movieValidator).validateMovieId(anyLong());
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // then
        assertThrows(MovieNotFoundException.class, () -> movieService.getMovieById(movieId));
    }

    @Test
    public void 영화단건_저장_테스트() {
        MovieRequest request = new MovieRequest("영화명", 2002, 1L);
        Movie movie = new Movie("영화명", 2002);

        doNothing().when(movieValidator).validateMovieRequest(any(MovieRequest.class));
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);
        doNothing().when(movieKafkaProducer).publishMovieCreated(any(), anyString());

        movieService.saveMovie(request);

        verify(movieRepository).save(any(Movie.class));
        verify(movieKafkaProducer).publishMovieCreated(any(), anyString());
    }
}
