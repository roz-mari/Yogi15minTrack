package com.yogi15mintrack.yogi15mintrack.exceptions;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(String entity, String field, String value) {
        super(entity + " with " + field + " = '" + value + "' not found");
    }
}
