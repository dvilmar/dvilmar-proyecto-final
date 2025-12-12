package com.bookmycut.exception;

/**
 * Excepción lanzada cuando hay un conflicto con el estado actual del recurso.
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}







