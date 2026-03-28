package com.geoedu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {

    private String baseUrl = "http://localhost:11434";
    private String model = "qwen2.5:7b";
    private String embeddingModel = "nomic-embed-text";
}
