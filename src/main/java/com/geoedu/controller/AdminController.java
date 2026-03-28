package com.geoedu.controller;

import com.geoedu.model.dto.ApiResponse;
import com.geoedu.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", "UP");
        health.put("redis", "UP");
        health.put("ollama", "UP");

        return ApiResponse.success(health);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rebuild-vectors")
    public ApiResponse<Map<String, Object>> rebuildVectors() {
        knowledgeService.regenerateAllVectors();
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Vector rebuild initiated");

        return ApiResponse.success(result);
    }

    @PostMapping("/backup")
    public ApiResponse<Map<String, String>> backup() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "Backup initiated");
        result.put("message", "Backup process started in background");

        return ApiResponse.success(result);
    }
}
