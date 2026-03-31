package com.example.domain.entity;

import com.example.domain.enums.MovieStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movie")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "production_year")
    private int productionYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MovieStatus status = MovieStatus.PRE_RELEASE;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Director director;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private List<Actor> actors;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private List<Investor> investors;

    public Movie(String name, int productionYear) {
        this.name = name;
        this.productionYear = productionYear;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void release(Double rating) {
        if (!this.status.canRelease()) {
            throw new IllegalStateException("Cannot release movie in status: " + this.status);
        }
        this.status = MovieStatus.RELEASED;
        this.rating = rating;
        this.releasedAt = LocalDateTime.now();
    }

    public void cancelRelease() {
        if (this.status != MovieStatus.RELEASED) {
            throw new IllegalStateException("Cannot cancel non-released movie");
        }
        this.status = MovieStatus.PRE_RELEASE;
        this.rating = null;
        this.releasedAt = null;
    }

    public void endShowing() {
        if (this.status != MovieStatus.RELEASED) {
            throw new IllegalStateException("Cannot end showing for non-released movie");
        }
        this.status = MovieStatus.END_OF_SHOWING;
    }
}
