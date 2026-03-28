package com.geoedu.exception;

public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String entityName, String id) {
        super(404, String.format("%s not found with id: %s", entityName, id));
    }

    public EntityNotFoundException(String message) {
        super(404, message);
    }
}
