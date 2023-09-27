package com.example.domain.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserRequest {
    private final String username;
    private final String password;
    private final String role;
}
