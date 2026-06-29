package com.projetoresgate.projetoresgate_api.shared.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Pattern(regexp = "^[1-9]{2}[2-5]\\d{7}$")
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Phone {

    String message() default "Número de telefone fixo inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}