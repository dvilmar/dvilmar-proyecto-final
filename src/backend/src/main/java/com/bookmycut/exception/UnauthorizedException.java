package com.bookmycut.exception;

/**
 * Excepción lanzada cuando el usuario no está autorizado para realizar una acción.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}







