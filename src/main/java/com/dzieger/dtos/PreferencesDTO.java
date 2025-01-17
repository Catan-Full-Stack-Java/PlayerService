package com.dzieger.dtos;

import java.util.Map;

public class PreferencesDTO {

    private Map<String, Object> preferences;

    public PreferencesDTO() {
    }

    public PreferencesDTO(Map<String, Object> preferences) {
        this.preferences = preferences;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }

}
