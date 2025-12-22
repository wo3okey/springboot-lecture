package com.example.repository;

import com.example.domain.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 이벤트 Repository
 *
 * [주요 기능]
 * - 미발행 이벤트 조회 (Polling)
 * - 발행 완료 처리
 * - 오래된 발행 완료 이벤트 삭제 (Cleanup)
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 미발행 이벤트 조회 (오래된 순서로)
     * Polling 방식에서 사용
     *
     * @param limit 조회할 최대 개수
     * @return 미발행 이벤트 목록
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.published = false ORDER BY o.createdAt ASC LIMIT :limit")
    List<OutboxEvent> findUnpublishedEvents(@Param("limit") int limit);

    /**
     * 발행 완료된 오래된 이벤트 삭제
     * 주기적인 Cleanup에서 사용
     *
     * @param before 이 시각 이전의 이벤트 삭제
     * @return 삭제된 레코드 수
     */
    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.published = true AND o.createdAt < :before")
    int deletePublishedEventsBefore(@Param("before") LocalDateTime before);

    /**
     * 특정 집계 타입의 미발행 이벤트 수 조회
     * 모니터링용
     *
     * @param aggregateType 집계 타입
     * @return 미발행 이벤트 수
     */
    @Query("SELECT COUNT(o) FROM OutboxEvent o WHERE o.aggregateType = :aggregateType AND o.published = false")
    long countUnpublishedByAggregateType(@Param("aggregateType") String aggregateType);
}
