package com.example.service;

import com.example.domain.entity.Movie;
import com.example.domain.request.MovieRequest;
import com.example.domain.response.MovieResponse;
import com.example.repository.MovieRepository;
import com.example.service.exception.MovieNotFoundException;
import com.example.service.validator.MovieValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 영화 관리 서비스
 *
 * 영화 조회, 생성, 수정, 삭제 등 영화 관련 비즈니스 로직을 처리합니다.
 *
 * Single Responsibility Principle: 영화 도메인의 비즈니스 로직에만 집중
 * Open/Closed Principle: 새로운 기능 추가 시 기존 코드 수정 최소화
 * Dependency Inversion Principle: Repository 인터페이스에 의존
 */
@Service
@RequiredArgsConstructor
public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movieRepository;
    private final LogService logService;
    private final MovieValidator movieValidator;

    /**
     * 영화 ID로 영화 정보를 조회합니다.
     *
     * @param movieId 조회할 영화 ID
     * @return 영화 응답 DTO
     * @throws MovieNotFoundException 영화를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long movieId) {
        movieValidator.validateMovieId(movieId);

        Movie movie = getMovieEntityByIdOrThrow(movieId);

        logger.debug("Successfully retrieved movie: {} (ID: {})", movie.getName(), movieId);
        return MovieResponse.of(movie);
    }

    /**
     * 영화 ID로 영화 엔티티를 조회합니다.
     *
     * @param movieId 조회할 영화 ID
     * @return 영화 엔티티
     * @throws MovieNotFoundException 영화를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Movie getMovieEntityById(Long movieId) {
        movieValidator.validateMovieId(movieId);
        return getMovieEntityByIdOrThrow(movieId);
    }

    /**
     * 모든 영화 목록을 조회합니다.
     *
     * @return 영화 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();

        logger.debug("Successfully retrieved {} movies", movies.size());
        return convertToMovieResponseList(movies);
    }

    /**
     * N+1 문제를 재현하기 위한 영화 목록 조회 (학습용)
     *
     * 이 메서드는 의도적으로 N+1 문제를 발생시켜 학습 목적으로 사용됩니다.
     *
     * @return 영화 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMoviesWithMultiFetchError() {
        List<Movie> movies = movieRepository.findAllMultiFetchError();

        logger.warn("Retrieved {} movies using multi-fetch error query (N+1 problem demonstration)",
                movies.size());
        return convertToMovieResponseList(movies);
    }

    /**
     * 새로운 영화를 저장합니다.
     *
     * @param movieRequest 영화 생성 요청 DTO
     * @throws IllegalArgumentException 유효하지 않은 요청인 경우
     */
    @Transactional
    public void saveMovie(MovieRequest movieRequest) {
        movieValidator.validateMovieRequest(movieRequest);

        Movie movie = buildMovieEntity(movieRequest);
        movieRepository.save(movie);

        logService.saveLog();

        logger.info("Successfully created movie: {} (Year: {})",
                movieRequest.getName(), movieRequest.getProductionYear());
    }

    /**
     * 영화 정보를 수정합니다.
     *
     * @param movieId 수정할 영화 ID
     * @param movieRequest 영화 수정 요청 DTO
     * @throws MovieNotFoundException 영화를 찾을 수 없는 경우
     * @throws IllegalArgumentException 유효하지 않은 요청인 경우
     */
    @Transactional
    public void updateMovie(Long movieId, MovieRequest movieRequest) {
        movieValidator.validateMovieId(movieId);
        movieValidator.validateMovieRequest(movieRequest);

        Movie movie = getMovieEntityByIdOrThrow(movieId);
        movie.updateName(movieRequest.getName());

        logger.info("Successfully updated movie: {} (ID: {})", movie.getName(), movieId);
    }

    /**
     * 영화를 삭제합니다.
     *
     * @param movieId 삭제할 영화 ID
     * @throws MovieNotFoundException 영화를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteMovie(Long movieId) {
        movieValidator.validateMovieId(movieId);

        verifyMovieExists(movieId);
        movieRepository.deleteById(movieId);

        logger.info("Successfully deleted movie with ID: {}", movieId);
    }

    /**
     * 영화 ID로 영화 엔티티를 조회하며, 존재하지 않을 경우 예외를 발생시킵니다.
     *
     * @param movieId 조회할 영화 ID
     * @return 조회된 영화 엔티티
     * @throws MovieNotFoundException 영화를 찾을 수 없는 경우
     */
    private Movie getMovieEntityByIdOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));
    }

    /**
     * 영화가 존재하는지 검증하고, 존재하지 않을 경우 예외를 발생시킵니다.
     *
     * @param movieId 검증할 영화 ID
     * @throws MovieNotFoundException 영화가 존재하지 않는 경우
     */
    private void verifyMovieExists(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(movieId);
        }
    }

    /**
     * MovieRequest로부터 Movie 엔티티를 생성합니다.
     *
     * @param movieRequest 영화 생성 요청 DTO
     * @return 생성된 Movie 엔티티
     */
    private Movie buildMovieEntity(MovieRequest movieRequest) {
        return new Movie(movieRequest.getName(), movieRequest.getProductionYear());
    }

    /**
     * Movie 엔티티 리스트를 MovieResponse 리스트로 변환합니다.
     *
     * @param movies Movie 엔티티 리스트
     * @return MovieResponse 리스트
     */
    private List<MovieResponse> convertToMovieResponseList(List<Movie> movies) {
        return movies.stream()
                .map(MovieResponse::of)
                .collect(Collectors.toList());
    }
}
