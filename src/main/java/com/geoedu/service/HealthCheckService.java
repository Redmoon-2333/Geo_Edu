package com.geoedu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final JdbcTemplate jdbcTemplate;
    private final RedisConnectionFactory redisConnectionFactory;
    private final ChatClient chatClient;

    public Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM knowledge", Long.class);
            result.put("status", "UP");
            result.put("knowledgeCount", count);
            result.put("message", "Database connection successful");
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            result.put("status", "DOWN");
            result.put("message", "Database connection failed: " + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> checkRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            String pong = redisConnectionFactory.getConnection().ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                result.put("status", "UP");
                result.put("message", "Redis connection successful");
            } else {
                result.put("status", "DOWN");
                result.put("message", "Redis ping returned: " + pong);
            }
        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage());
            result.put("status", "DOWN");
            result.put("message", "Redis connection failed: " + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> checkOllama() {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = chatClient.prompt()
                    .user("ping")
                    .call()
                    .content();
            
            if (response != null && !response.isEmpty()) {
                result.put("status", "UP");
                result.put("message", "Ollama service is responding");
            } else {
                result.put("status", "DOWN");
                result.put("message", "Ollama returned empty response");
            }
        } catch (Exception e) {
            log.error("Ollama health check failed: {}", e.getMessage());
            result.put("status", "DOWN");
            result.put("message", "Ollama service unavailable: " + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> checkAll() {
        Map<String, Object> health = new HashMap<>();
        
        Map<String, Object> dbHealth = checkDatabase();
        Map<String, Object> redisHealth = checkRedis();
        Map<String, Object> ollamaHealth = checkOllama();
        
        boolean allUp = "UP".equals(dbHealth.get("status"))
                && "UP".equals(redisHealth.get("status"))
                && "UP".equals(ollamaHealth.get("status"));
        
        health.put("status", allUp ? "UP" : "DEGRADED");
        health.put("database", dbHealth);
        health.put("redis", redisHealth);
        health.put("ollama", ollamaHealth);
        health.put("timestamp", System.currentTimeMillis());
        
        return health;
    }
}
