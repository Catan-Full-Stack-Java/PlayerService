package com.dzieger.validations;

import com.dzieger.annotations.ValidPreferences;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class PreferenceValidator implements ConstraintValidator<ValidPreferences, Map<String, Object>> {

    @Override
    public boolean isValid(Map<String, Object> preferences, ConstraintValidatorContext context) {
        if (preferences == null) {
            return false;
        }
        for (Map.Entry<String, Object> entry : preferences.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                return false;
            }
        }
        return true;
    }

}
