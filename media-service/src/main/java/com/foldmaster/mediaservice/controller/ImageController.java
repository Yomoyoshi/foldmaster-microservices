package com.foldmaster.mediaservice.controller;

import com.foldmaster.common.dto.ApiResponse;
import com.foldmaster.mediaservice.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String filename = fileStorageService.store(file);
        // Возвращаем URL для доступа к изображению
        String imageUrl = "/api/images/" + filename;
        return ResponseEntity.ok(ApiResponse.success("Image uploaded", imageUrl));
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        List<String> urls = files.stream()
                .map(file -> {
                    String filename = fileStorageService.store(file);
                    return "/api/images/" + filename;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Images uploaded", urls));
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        byte[] data = fileStorageService.loadAsBytes(filename);
        // Определяем content-type по расширению (можно улучшить)
        String contentType = MediaType.IMAGE_JPEG_VALUE; // по умолчанию
        if (filename.endsWith(".png")) {
            contentType = MediaType.IMAGE_PNG_VALUE;
        } else if (filename.endsWith(".gif")) {
            contentType = MediaType.IMAGE_GIF_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable String filename) {
        fileStorageService.delete(filename);
        return ResponseEntity.ok(ApiResponse.success("Image deleted", null));
    }
}