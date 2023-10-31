package org.babi.backend.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Class<?> entityClass, String fieldName, Object identifier) {
        super(String.format("Resource %s not found by %s: %s", entityClass.getCanonicalName(), fieldName, identifier.toString()));
    }
}
