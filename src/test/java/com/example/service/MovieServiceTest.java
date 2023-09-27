package com.example.service;

import com.example.domain.response.MovieResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class MovieServiceTest {
    @Autowired
    private MovieService movieService;

    @Test
    @DisplayName("영화 단건 조회 테스트")
    public void getMovieTest() {
        // given
        int movieId = 12323;

        // when
        MovieResponse movieResponse = movieService.getMovie(movieId);

        // then
        assertNotNull(movieResponse);
    }
}
