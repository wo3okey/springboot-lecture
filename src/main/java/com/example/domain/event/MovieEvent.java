package com.example.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka로 발행되는 영화 이벤트 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieEvent {

    private MovieEventType eventType;
    private Long movieId;
    private String movieName;
    private LocalDateTime occurredAt;

    /**
     * 영화 생성 이벤트 팩토리 메서드
     */
    public static MovieEvent created(Long movieId, String movieName) {
        return MovieEvent.builder()
                .eventType(MovieEventType.CREATED)
                .movieId(movieId)
                .movieName(movieName)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    /**
     * 영화 수정 이벤트 팩토리 메서드
     */
    public static MovieEvent updated(Long movieId, String movieName) {
        return MovieEvent.builder()
                .eventType(MovieEventType.UPDATED)
                .movieId(movieId)
                .movieName(movieName)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    /**
     * 영화 삭제 이벤트 팩토리 메서드
     */
    public static MovieEvent deleted(Long movieId) {
        return MovieEvent.builder()
                .eventType(MovieEventType.DELETED)
                .movieId(movieId)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    public String toString() {
        return String.format("MovieEvent{eventType=%s, movieId=%d, movieName='%s', occurredAt=%s}",
                eventType, movieId, movieName, occurredAt);
    }
}
