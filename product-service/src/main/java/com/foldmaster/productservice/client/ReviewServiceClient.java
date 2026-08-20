package com.foldmaster.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "review-service")
public interface ReviewServiceClient {

    @GetMapping("/api/reviews/product/{productId}/average")
    Double getAverageRating(@PathVariable("productId") Long productId);
}