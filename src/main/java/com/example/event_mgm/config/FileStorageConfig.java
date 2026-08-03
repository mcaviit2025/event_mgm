package com.example.event_mgm.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {

    private String uploadDir = "uploads/payments/";

    @PostConstruct
    public void init() {
        // Create upload directory if it doesn't exist
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }
}