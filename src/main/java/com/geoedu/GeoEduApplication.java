package com.geoedu;

import com.geoedu.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.geoedu.config.AppConfig;

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
@MapperScan("com.geoedu.mapper")
@RequiredArgsConstructor
@Slf4j
public class GeoEduApplication implements CommandLineRunner {

    private final KnowledgeService knowledgeService;

    public static void main(String[] args) {
        SpringApplication.run(GeoEduApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("Starting GeoEdu application initialization...");
        try {
            knowledgeService.generateMissingVectors();
            log.info("GeoEdu application initialization complete.");
        } catch (Exception e) {
            log.error("Error during application initialization: {}", e.getMessage(), e);
        }
    }
}