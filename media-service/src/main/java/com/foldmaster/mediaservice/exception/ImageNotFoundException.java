package com.foldmaster.mediaservice.exception;

import com.foldmaster.common.exception.ResourceNotFoundException;

public class ImageNotFoundException extends ResourceNotFoundException {
    public ImageNotFoundException(String message) {
        super(message);
    }

    public ImageNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName, fieldName, fieldValue);
    }
}