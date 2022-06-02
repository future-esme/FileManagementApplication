package com.esempla.file.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    private final AppProperties properties;

    public MinioConfiguration(AppProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MinioClient minioClient() {
        var minio = properties.getMinio();
        return new MinioClient.Builder()
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .endpoint(minio.getUrl())
                .build();
    }
}
