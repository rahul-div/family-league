package com.familyleague.common.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entity, UUID id) {
        return new ResourceNotFoundException(entity + " not found with id: " + id);
    }

    public static ResourceNotFoundException of(String entity, String key) {
        return new ResourceNotFoundException(entity + " not found with key: " + key);
    }
}
