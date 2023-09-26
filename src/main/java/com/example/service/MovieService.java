package com.example.service;

import com.example.domain.entity.Movie;
import com.example.domain.request.MovieRequest;
import com.example.domain.response.MovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final EntityManagerFactory emf;

    public List<MovieResponse> getMovies() {
        return null;
    }

    public MovieResponse getMovie(long movieId) {
        EntityManager em = emf.createEntityManager();
        Movie movie = em.find(Movie.class, movieId);
        return MovieResponse.of(movie);
    }

    public void saveMovie(MovieRequest movieRequest) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Movie newMovie = new Movie(movieRequest.getName() + "1", movieRequest.getProductionYear());
            Movie newMovie2 = new Movie(movieRequest.getName() + "2", movieRequest.getProductionYear());
            em.persist(newMovie);
            em.persist(newMovie2);

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
        em.close();
    }

    /**
     * 1. 쓰기지연: set 해도 바로 업뎃안됨
     * 2. dirty checking: update 쿼리 없이 set으로 업데이트 쿼리날림
     * 3. 1차 캐시: movie를 두번 호출했지만, 같은 아이디로 호출하여 1번만 select함
     */
    public void updateMovie(long movieId, MovieRequest movieRequest) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Movie movie = em.find(Movie.class, movieId);
            movie.setName(movieRequest.getName());

            Movie movie2 = em.find(Movie.class, movieId);
            movie2.setName(movieRequest.getName() + "2");

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
        em.close();
    }

    /**
     * 1. entity의 orphanRemoval=true: movie 날리면 연관된 actor 날라감.
     */
    public void removeMovie(long movieId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Movie movie = em.find(Movie.class, movieId);
            em.remove(movie);

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
        em.close();
    }
}
