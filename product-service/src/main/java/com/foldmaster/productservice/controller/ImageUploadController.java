package com.foldmaster.productservice.controller;

import com.foldmaster.common.dto.ApiResponse;
import com.foldmaster.productservice.entity.Product;
import com.foldmaster.productservice.exception.ProductNotFoundException;
import com.foldmaster.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ImageUploadController {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> files) throws IOException {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Images list cannot be empty"));
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        for (MultipartFile file : files) {
            HttpHeaders headers = new HttpHeaders();
            // НЕ устанавливаем Content-Type вручную – Spring сам добавит boundary

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Отправляем запрос и получаем ответ как Map
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = restTemplate.postForObject(
                    "http://media-service:8085/api/images/upload",
                    requestEntity,
                    Map.class
            );

            if (responseMap == null) {
                log.error("Empty response from media-service for product {}", id);
                continue;
            }

            Boolean success = (Boolean) responseMap.get("success");
            if (success != null && success) {
                String imageUrl = (String) responseMap.get("data");
                if (imageUrl != null) {
                    product.getImages().add(imageUrl);
                    log.info("Image uploaded for product {}: {}", id, imageUrl);
                } else {
                    log.warn("Image uploaded but data field is null for product {}", id);
                }
            } else {
                String message = (String) responseMap.get("message");
                log.error("Failed to upload image for product {}: {}", id, message);
            }
        }

        productRepository.save(product);

        return ResponseEntity.ok(ApiResponse.success(
                "Images uploaded successfully",
                Map.of("images", product.getImages())
        ));
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long id,
            @RequestParam String imageUrl) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        if (product.getImages().remove(imageUrl)) {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            restTemplate.delete("http://media-service:8085/api/images/" + filename);
            productRepository.save(product);
            return ResponseEntity.ok(ApiResponse.success("Image deleted", null));
        } else {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Image not found for this product"));
        }
    }
}