package com.geoedu.controller;

import com.geoedu.exception.AuthenticationException;
import com.geoedu.model.dto.ApiResponse;
import com.geoedu.model.dto.LoginRequest;
import com.geoedu.model.dto.LoginResponse;
import com.geoedu.model.dto.RegisterRequest;
import com.geoedu.model.dto.UserDTO;
import com.geoedu.service.AuthService;
import com.geoedu.util.AuthenticationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return ApiResponse.success(user);
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser() {
        try {
            String userId = AuthenticationUtils.getCurrentUserId();
            UserDTO user = authService.getUserById(userId);
            return ApiResponse.success(user);
        } catch (AuthenticationException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }
}
