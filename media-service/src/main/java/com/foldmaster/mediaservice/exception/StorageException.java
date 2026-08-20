package com.foldmaster.mediaservice.exception;

import com.foldmaster.common.exception.ResourceNotFoundException;

public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
