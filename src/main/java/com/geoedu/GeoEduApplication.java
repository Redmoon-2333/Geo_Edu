package com.geoedu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.geoedu.config.AppConfig;

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
@MapperScan("com.geoedu.mapper")
public class GeoEduApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeoEduApplication.class, args);
    }
}