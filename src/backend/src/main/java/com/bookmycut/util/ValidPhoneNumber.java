package com.bookmycut.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación personalizada para validar números de teléfono.
 * Acepta formatos españoles con 9 dígitos.
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    
    String message() default "El teléfono debe tener un formato válido (9 dígitos, opcional prefijo +34 o 0034)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}





