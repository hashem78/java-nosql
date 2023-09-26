package me.hashemalayan.services.db.exceptions;

public class InvalidCollectionSchemaException extends RuntimeException {
    public InvalidCollectionSchemaException(String message) {
        super(message);
    }
}
