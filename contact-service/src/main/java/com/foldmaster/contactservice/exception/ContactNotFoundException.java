package com.foldmaster.contactservice.exception;

import com.foldmaster.common.exception.ResourceNotFoundException;

public class ContactNotFoundException extends ResourceNotFoundException {

    public ContactNotFoundException(String message) {
        super(message);
    }

    public ContactNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName, fieldName, fieldValue);
    }
}