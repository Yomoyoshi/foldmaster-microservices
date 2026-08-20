package com.foldmaster.productservice.service;

import com.foldmaster.productservice.client.ReviewServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductClientService {

    private final ReviewServiceClient reviewServiceClient;

    public Double getAverageRating(Long productId) {
        try {
            return reviewServiceClient.getAverageRating(productId);
        } catch (Exception e) {
            log.warn("Could not fetch average rating for product {}: {}", productId, e.getMessage());
            return 0.0;
        }
    }
}