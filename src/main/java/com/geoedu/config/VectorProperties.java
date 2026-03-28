package com.geoedu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.vector")
public class VectorProperties {

    private int dimension = 768;

    private int searchTopK = 3;
}
