package com.example.common;

import com.example.service.exception.MovieNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleMovieNotFoundException(MovieNotFoundException e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
            HttpStatus.NOT_FOUND.value(),
            e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
            HttpStatus.BAD_REQUEST.value(),
            e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ExceptionResponse> handleException(NoSuchElementException e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "데이터좀 잘좀 빼갑시다~!"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
