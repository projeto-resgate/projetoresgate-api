package com.projetoresgate.projetoresgate_api.shared.validation.validator;

import com.projetoresgate.projetoresgate_api.shared.validation.annotation.RG;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;

public class RGValidator implements ConstraintValidator<RG, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isNull(value) || value.isBlank()) {
            return true;
        }

        String normalized = value.replaceAll("[^0-9A-Za-z]", "").toUpperCase();
        return normalized.matches("^([A-Z]{2})?\\d{4,13}[0-9X]$");
    }
}
