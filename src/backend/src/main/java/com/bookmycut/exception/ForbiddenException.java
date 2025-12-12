package com.bookmycut.exception;

/**
 * Excepción lanzada cuando el usuario no tiene permisos suficientes para realizar una acción.
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}







