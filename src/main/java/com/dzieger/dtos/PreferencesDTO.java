package com.dzieger.dtos;

import com.dzieger.annotations.ValidPreferences;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class PreferencesDTO {

    private Map<String, Object> preferences;

    public PreferencesDTO() {
    }

    // Constructor
    public PreferencesDTO(Map<String, Object> preferences) {
        this.preferences = preferences;
    }

    // Getters and setters
    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }
}
