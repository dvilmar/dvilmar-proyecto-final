package com.bookmycut.exception;

/**
 * Excepción lanzada cuando se realiza una petición con datos inválidos.
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}







