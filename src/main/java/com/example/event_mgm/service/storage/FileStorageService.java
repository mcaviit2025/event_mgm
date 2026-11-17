package com.example.event_mgm.service.storage;

import com.example.event_mgm.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import com.example.event_mgm.exceptions.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    /**
     * Store payment screenshot and return the file path
     */
    public String storePaymentScreenshot(MultipartFile file, Long registrationId) {
        try {
            // Validate file
            validateFile(file);

            // Generate unique filename
            String filename = generateFilename(file, registrationId);

            // Get upload path
            Path uploadPath = Paths.get(fileStorageConfig.getUploadDir());

            // Create directory if not exists
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            Path filePath = uploadPath.resolve(filename);
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Payment screenshot saved: {}", filePath.toString());

            return filePath.toString();

        } catch (IOException e) {
            log.error("Failed to store payment screenshot: {}", e.getMessage());
            throw new RuntimeException("Failed to store payment screenshot", e);
        }
    }

    /**
     * Validate file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        // Check file extensions
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: JPG, JPEG, PNG, GIF");
        }
    }

    /**
     * Generate unique filename
     */
    private String generateFilename(MultipartFile file, Long registrationId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("payment_%d_%s_%s.%s", registrationId, dateTime, uuid, extension);
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Check if extension is valid
     */
    private boolean isValidImageExtension(String extension) {
        return extension.matches("(?i)(jpg|jpeg|png|gif)");
    }

    /**
     * Delete file
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath, e);
        }
    }

    public byte[] readPaymentScreenshot(String filePath) {
        Path root = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        Path screenshotPath = Paths.get(filePath).toAbsolutePath().normalize();
        if (!screenshotPath.startsWith(root) || !Files.isRegularFile(screenshotPath)) {
            throw new ResourceNotFoundException("Payment screenshot not found");
        }
        try {
            return Files.readAllBytes(screenshotPath);
        } catch (IOException e) {
            log.error("Failed to read payment screenshot", e);
            throw new RuntimeException("Failed to read payment screenshot", e);
        }
    }

    public MediaType getScreenshotMediaType(String filePath) {
        String extension = getFileExtension(filePath).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            default -> throw new IllegalArgumentException("Unsupported payment screenshot type");
        };
    }
}