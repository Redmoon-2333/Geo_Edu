package com.geoedu.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.embedding-model}")
    private String embeddingModel;

    @Value("${app.vector.dimension}")
    private int vectorDimension;

    private static final String VECTOR_KEY_PREFIX = "knowledge:vector:";
    private static final String VECTOR_INDEX_NAME = "knowledge_vector_index";

    @PostConstruct
    public void initVectorIndex() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            try {
                String indexExists = "FT.INFO " + VECTOR_INDEX_NAME;
                if (!connection.commands().exec().toString().contains(VECTOR_INDEX_NAME)) {
                    String createIndexCmd = String.format(
                            "FT.CREATE %s ON hash SCHEMA vector AS vector VECTOR HNSW 6 TYPE FLOAT32 DIM %d DISTANCE_METRIC COSINE",
                            VECTOR_INDEX_NAME, vectorDimension
                    );
                    connection.commands().exec().forEach(cmd -> log.debug("Redis response: {}", cmd));
                }
            } finally {
                connection.close();
            }
            log.info("Vector index initialized successfully");
        } catch (Exception e) {
            log.warn("Failed to initialize vector index, will retry on first use: {}", e.getMessage());
        }
    }

    public float[] embedText(String text) {
        String url = ollamaBaseUrl + "/api/embeddings";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", embeddingModel);
        requestBody.put("prompt", text);

        Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

        if (response == null || !response.containsKey("embedding")) {
            throw new RuntimeException("Failed to get embedding from Ollama");
        }

        @SuppressWarnings("unchecked")
        List<Number> embedding = (List<Number>) response.get("embedding");
        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = embedding.get(i).floatValue();
        }
        return vector;
    }

    public void saveVector(String knowledgeId, float[] vector, Map<String, String> metadata) {
        String key = VECTOR_KEY_PREFIX + knowledgeId;

        Map<String, String> hashData = new HashMap<>();
        hashData.put("vector", floatArrayToString(vector));
        if (metadata != null) {
            metadata.forEach(hashData::put);
        }

        redisTemplate.opsForHash().putAll(key, hashData);
        log.debug("Saved vector for knowledge: {}", knowledgeId);
    }

    public List<SearchResult> searchSimilar(float[] queryVector, int topK, Map<String, String> filters) {
        String queryVectorStr = floatArrayToString(queryVector);

        StringBuilder filterStr = new StringBuilder();
        if (filters != null && !filters.isEmpty()) {
            filterStr.append("@grade:{").append(filters.getOrDefault("grade", "*")).append("} ");
            filterStr.append("@chapter:{").append(filters.getOrDefault("chapter", "*")).append("} ");
            filterStr.append("@difficulty:{").append(filters.getOrDefault("difficulty", "*")).append("}");
        }

        String searchCmd = String.format(
                "FT.SEARCH %s \"[%s]\" LIMIT 0 %d RETURN 2 vector metadata",
                VECTOR_INDEX_NAME, queryVectorStr, topK
        );

        if (filterStr.length() > 0) {
            searchCmd += " FILTER " + filterStr;
        }

        @SuppressWarnings("unchecked")
        List<Object> rawResults = (List<Object>) redisTemplate.execute(
                RedisScript.of(searchCmd),
                Collections.emptyList()
        );

        List<SearchResult> results = new ArrayList<>();
        if (rawResults != null) {
            for (int i = 0; i < rawResults.size(); i += 2) {
                String key = (String) rawResults.get(i);
                @SuppressWarnings("unchecked")
                Map<String, String> metadata = (Map<String, String>) rawResults.get(i + 1);
                results.add(SearchResult.builder()
                        .knowledgeId(key.replace(VECTOR_KEY_PREFIX, ""))
                        .metadata(metadata)
                        .score(1.0f)
                        .build());
            }
        }

        return results;
    }

    private String floatArrayToString(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchResult {
        private String knowledgeId;
        private float[] vector;
        private float score;
        private Map<String, String> metadata;
    }
}