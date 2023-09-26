package com.example.repository;

import com.example.domain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Query("SELECT distinct m FROM Movie m LEFT JOIN FETCH m.actors a")
    List<Movie> findAllJpqlFetch();

    List<Movie> findByProductionYear(int productionYear);

    // multi fetch error 발생
    // @Query("SELECT distinct m FROM Movie m LEFT JOIN FETCH m.actors a LEFT JOIN FETCH m.investor i")
}
