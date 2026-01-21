package com.example.outbound.recommendation;

public interface RecommendationClient {

    void indexMovie(Long movieId, String movieName, Double rating);

    void removeMovie(Long movieId);
}
