package com.example.outbound.rating;

import com.example.domain.response.RatingResponse;

public interface RatingClient {

    RatingResponse getRating(Long movieId, String movieName);
}
