package com.dzieger.annotations;

import com.dzieger.validations.PreferenceValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PreferenceValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPreferences {
    String message() default "Invalid preferences";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}
