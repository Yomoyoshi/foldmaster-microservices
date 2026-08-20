package com.foldmaster.reviewservice.exception;

import com.foldmaster.common.exception.ResourceNotFoundException;

public class ReviewNotFoundException extends ResourceNotFoundException {

    public ReviewNotFoundException(String message) {
        super(message);
    }

    public ReviewNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName, fieldName, fieldValue);
    }
}