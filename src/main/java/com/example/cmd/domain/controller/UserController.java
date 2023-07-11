package com.example.cmd.domain.controller;

import com.example.cmd.domain.controller.dto.request.LoginRequest;
import com.example.cmd.domain.controller.dto.request.UserSignupRequest;
import com.example.cmd.domain.controller.dto.response.UserInfoResponse;
import com.example.cmd.domain.service.UserService;
import com.example.cmd.global.security.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("signup")
    public void signup(@RequestBody UserSignupRequest signupRequest) {
        userService.userSignUp(signupRequest);
    }

    @PostMapping("login")
    public Token login(@RequestBody LoginRequest loginRequest) {
        return userService.userLogin(loginRequest);
    }

    @GetMapping("myPage")
    public UserInfoResponse myPage() {
        return userService.myPage();
    }

}