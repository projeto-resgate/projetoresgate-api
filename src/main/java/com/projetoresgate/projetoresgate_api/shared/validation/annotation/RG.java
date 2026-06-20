package com.projetoresgate.projetoresgate_api.shared.validation.annotation;

import com.projetoresgate.projetoresgate_api.shared.validation.validator.RGValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RGValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RG {

    String message() default "RG inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}