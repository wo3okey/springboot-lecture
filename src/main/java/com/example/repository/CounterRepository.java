package com.example.repository;

import com.example.domain.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface CounterRepository extends JpaRepository<Counter, Long> {
    long FIRST = 1;
    default Counter findFirstElseThrow() {
        return this.findById(FIRST).orElseThrow();
    }

    default Counter findFirstElseThrowForUpdate() {
        return this.findByIdForUpdate(FIRST).orElseThrow();
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Counter c where c.id = :id")
    Optional<Counter> findByIdForUpdate(Long id);
}
