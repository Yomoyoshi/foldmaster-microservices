package com.foldmaster.mediaservice.service;

import com.foldmaster.mediaservice.config.StorageProperties;
import com.foldmaster.mediaservice.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path rootLocation;

    @Autowired
    public FileStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                log.info("Created upload directory: {}", rootLocation.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location", e);
        }
    }

    /**
     * Сохраняет файл и возвращает уникальное имя файла.
     */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }
        // Генерируем уникальное имя файла
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            Path destination = this.rootLocation.resolve(storedFilename);
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored: {}", storedFilename);
            return storedFilename;
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + storedFilename, e);
        }
    }

    /**
     * Загружает файл как массив байтов.
     */
    public byte[] loadAsBytes(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            if (!Files.exists(file)) {
                throw new StorageException("File not found: " + filename);
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new StorageException("Failed to read file: " + filename, e);
        }
    }

    /**
     * Удаляет файл.
     */
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            if (Files.exists(file)) {
                Files.delete(file);
                log.info("File deleted: {}", filename);
            } else {
                throw new StorageException("File not found: " + filename);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + filename, e);
        }
    }

    /**
     * Проверяет существование файла.
     */
    public boolean exists(String filename) {
        return Files.exists(rootLocation.resolve(filename));
    }
}