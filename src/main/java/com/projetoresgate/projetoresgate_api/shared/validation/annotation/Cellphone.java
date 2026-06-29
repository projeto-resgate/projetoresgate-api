package com.projetoresgate.projetoresgate_api.shared.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Pattern(
        regexp = "^[1-9]{2}9\\d{8}$",
        message = "Número de celular inválido. Utilize o formato DDD + número (ex: 47999998888)"
)
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cellphone {

    String message() default "Número de celular inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}