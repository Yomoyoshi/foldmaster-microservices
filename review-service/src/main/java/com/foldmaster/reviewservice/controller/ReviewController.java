package com.foldmaster.reviewservice.controller;

import com.foldmaster.common.dto.ApiResponse;
import com.foldmaster.reviewservice.dto.ReviewRequest;
import com.foldmaster.reviewservice.entity.Review;
import com.foldmaster.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByProduct(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAverageRating(@PathVariable Long productId) {
        Double avg = reviewService.getAverageRating(productId);
        long total = reviewService.countByProductId(productId);
        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", avg != null ? avg : 0.0);
        result.put("totalReviews", total);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Review>> getReview(@PathVariable Long id) {
        Review review = reviewService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(@Valid @RequestBody ReviewRequest request) {
        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setRating(request.getRating());
        review.setComment(request.getText());
        review.setUserId(request.getUserId());
        review.setAuthorName(request.getAuthorName());
        review.setCreatedAt(LocalDateTime.now());

        Review created = reviewService.createReview(review);
        return ResponseEntity.ok(ApiResponse.success("Review created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Review>> updateReview(@PathVariable Long id,
                                                            @RequestBody Review review) {
        Review updated = reviewService.updateReview(id, review);
        return ResponseEntity.ok(ApiResponse.success("Review updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}