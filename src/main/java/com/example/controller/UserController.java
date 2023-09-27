package com.example.controller;

import com.example.domain.request.UserRequest;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/api/v1/user/join")
    public void join(
            @RequestBody UserRequest userRequest
    ) {
        userService.join(userRequest);
    }

    @GetMapping("/home")
    public String home() {
        return "어서와^^ 로그인 성공했네~ ㅊㅋㅊㅋ";
    }

    @GetMapping("/admin")
    public String admin() {
        return "어드민만 접속한 가능한 화면에 용케 들어왔네^^~";
    }

    @GetMapping("/manager")
    public String manger() {
        return "매니저까지 접속 가능한 화면에 용케 들어왔네^^~";
    }
}
