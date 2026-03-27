package com.geoedu.controller;

import com.geoedu.config.JwtTokenProvider;
import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.LoginRequest;
import com.geoedu.model.dto.LoginResponse;
import com.geoedu.model.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 生成真实的 JWT Token
        String token = jwtTokenProvider.generateToken(request.getUsername(), UUID.randomUUID().toString());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .expiresIn(86400)
                .user(UserDTO.builder()
                        .username(request.getUsername())
                        .role("student")
                        .build())
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody LoginRequest request) {
        // 生成真实的 JWT Token
        String token = jwtTokenProvider.generateToken(request.getUsername(), UUID.randomUUID().toString());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .expiresIn(86400)
                .user(UserDTO.builder()
                        .username(request.getUsername())
                        .role("student")
                        .build())
                .build();

        return ApiResponse.success(response);
    }
}
