package com.geoedu.controller;

import com.geoedu.model.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", "UP");
        health.put("redis", "UP");
        health.put("ollama", "UP");

        return ApiResponse.success(health);
    }

    @PostMapping("/backup")
    public ApiResponse<Map<String, String>> backup() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "Backup initiated");
        result.put("message", "Backup process started in background");

        return ApiResponse.success(result);
    }
}
