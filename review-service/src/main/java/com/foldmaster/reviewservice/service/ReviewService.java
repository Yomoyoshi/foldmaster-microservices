package com.foldmaster.reviewservice.service;

import com.foldmaster.reviewservice.client.ProductServiceClient;
import com.foldmaster.reviewservice.client.UserServiceClient;
import com.foldmaster.reviewservice.entity.Review;
import com.foldmaster.reviewservice.exception.ReviewNotFoundException;
import com.foldmaster.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Double getAverageRating(Long productId) {
        return reviewRepository.findAverageRatingByProductId(productId);
    }

    public long countByProductId(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    @Transactional
    public Review createReview(Review review) {
        // Можно добавить проверку на существование пользователя/продукта через Feign, если нужно
        return reviewRepository.save(review);
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
    }

    @Transactional
    public Review updateReview(Long id, Review updated) {
        Review existing = findById(id);
        if (updated.getRating() != null) {
            existing.setRating(updated.getRating());
        }
        if (updated.getComment() != null) {
            existing.setComment(updated.getComment());
        }
        return reviewRepository.save(existing);
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = findById(id);
        reviewRepository.delete(review);
    }
}