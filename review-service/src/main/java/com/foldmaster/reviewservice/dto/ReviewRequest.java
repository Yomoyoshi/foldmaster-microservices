package com.foldmaster.reviewservice.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long productId;
    private Integer rating;
    private String text;
    private Long userId;
    private String authorName;
}
