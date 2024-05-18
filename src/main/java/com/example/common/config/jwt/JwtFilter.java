package com.example.common.config.jwt;

import com.example.domain.entity.User;
import com.example.domain.response.UserResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        // header 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.error("token is null");
            filterChain.doFilter(request, response);
            return;
        }

        // token expired 검증
        String token = authorization.split(" ")[1];
        if (jwtUtils.isExpired(token)) {
            log.error("token expired");
            filterChain.doFilter(request, response);
            return;
        }

        // token에서 user 정보 획득
        User entity = new User(jwtUtils.getUsername(token), "temp", jwtUtils.getRole(token));
        UserResponse user = UserResponse.of(entity);

        // security 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // 세션에서 사용자 획득
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
