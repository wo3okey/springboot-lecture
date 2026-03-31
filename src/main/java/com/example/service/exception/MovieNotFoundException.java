package com.example.service.exception;

/**
 * 영화를 찾을 수 없을 때 발생하는 예외
 */
public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(Long movieId) {
        super("Movie not found with id: " + movieId);
    }

    public MovieNotFoundException(String message) {
        super(message);
    }
}
