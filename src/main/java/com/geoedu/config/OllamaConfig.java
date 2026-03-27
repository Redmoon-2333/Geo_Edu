package com.geoedu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {

    private String baseUrl = "http://localhost:11434";
    private String model = "qwen2.5:7b";
    private String embeddingModel = "nomic-embed-text";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebClient ollamaWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Map<String, Object> generate(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        return ollamaWebClient()
                .post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public double[] embeddings(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", embeddingModel,
                "prompt", prompt
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = ollamaWebClient()
                .post()
                .uri("/api/embeddings")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("embedding")) {
            throw new RuntimeException("Invalid embedding response from Ollama");
        }

        @SuppressWarnings("unchecked")
        java.util.List<Number> embeddingList = (java.util.List<Number>) response.get("embedding");
        double[] result = new double[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            result[i] = embeddingList.get(i).doubleValue();
        }
        return result;
    }
}
