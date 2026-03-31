package com.example.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbox 이벤트 엔티티
 *
 * [Transactional Outbox Pattern]
 * - DB 트랜잭션과 Kafka 발행의 원자성을 보장하기 위한 패턴
 * - 비즈니스 데이터와 이벤트를 같은 트랜잭션에서 저장
 * - 별도 프로세스가 Outbox를 폴링하여 Kafka로 발행
 *
 * [장점]
 * - 데이터 정합성 보장 (DB 저장 실패 시 이벤트도 롤백)
 * - At-Least-Once 전달 보장
 * - 장애 복구 용이 (미발행 이벤트 재처리 가능)
 */
@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 집계 타입 (엔티티 종류)
     * 예: "Movie", "Order", "User"
     */
    @Column(nullable = false)
    private String aggregateType;

    /**
     * 집계 ID (엔티티 ID)
     * 예: "123", "456"
     */
    @Column(nullable = false)
    private String aggregateId;

    /**
     * 이벤트 타입
     * 예: "CREATED", "UPDATED", "DELETED"
     */
    @Column(nullable = false)
    private String eventType;

    /**
     * 이벤트 페이로드 (JSON)
     * 실제 이벤트 데이터를 JSON 문자열로 저장
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    /**
     * 이벤트 생성 시각
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Kafka 발행 완료 여부
     */
    @Column(nullable = false)
    private boolean published;

    /**
     * Outbox 이벤트 생성
     */
    public OutboxEvent(String aggregateType, String aggregateId,
                       String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.published = false;
    }

    /**
     * 발행 완료 처리
     */
    public void markAsPublished() {
        this.published = true;
    }

    /**
     * 정적 팩토리 메서드
     */
    public static OutboxEvent of(String aggregateType, String aggregateId,
                                  String eventType, String payload) {
        return new OutboxEvent(aggregateType, aggregateId, eventType, payload);
    }
}
