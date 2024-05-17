package com.example.domain.request;

import com.example.domain.enums.MemberRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserRequest {
    private final String username;
    private final String password;
    private final MemberRole role;
}
