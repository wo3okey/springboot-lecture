package com.example.common;

import lombok.Getter;

@Getter
public class Response<T> {
    private final T data;

    public Response(T data) {
        this.data = data;
    }

    public static <T> Response<T> of(T data) {
        return new Response<>(data);
    }
}