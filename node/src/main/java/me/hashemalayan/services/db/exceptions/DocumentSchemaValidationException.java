package me.hashemalayan.services.db.exceptions;

public class DocumentSchemaValidationException extends RuntimeException {
    public DocumentSchemaValidationException(String message) {
        super(message);
    }
}
