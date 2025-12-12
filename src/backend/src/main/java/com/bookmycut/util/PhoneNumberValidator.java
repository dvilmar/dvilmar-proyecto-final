package com.bookmycut.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validador personalizado para números de teléfono.
 * Acepta formatos españoles:
 * - 9 dígitos (ej: 123456789)
 * - Prefijo opcional +34 o 0034 (ej: +34123456789, 0034123456789)
 * - Espacios opcionales entre dígitos (ej: 123 456 789)
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    
    // Patrón para teléfonos españoles: 9 dígitos, opcional prefijo +34 o 0034, espacios opcionales
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+34|0034)?[\\s]?[6-9][0-9]{8}$"
    );
    
    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // No se necesita inicialización
    }
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // Si es null o vacío, se permite (el teléfono puede ser opcional en algunos contextos)
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }
        
        // Eliminar espacios para validar
        String cleanPhone = phone.replaceAll("\\s", "");
        
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
}





