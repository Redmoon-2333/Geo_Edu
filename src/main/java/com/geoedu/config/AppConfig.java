package com.geoedu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private Vector vector = new Vector();
    private Search search = new Search();
    private Upload upload = new Upload();

    @Data
    public static class Vector {
        private int dimension = 768;
    }

    @Data
    public static class Search {
        private int topK = 3;
    }

    @Data
    public static class Upload {
        private String path = "/data/images";
        private String maxSize = "10MB";
    }
}
