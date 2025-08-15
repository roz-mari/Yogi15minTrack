package com.yogi15mintrack.yogi15mintrack.exceptions;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String entity, String field, String value) {
        super(entity + " with " + field + " = '" + value + "' already exists");
    }
}
