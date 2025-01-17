package com.dzieger.eventPayloadDtos;

import java.util.Map;
import java.util.UUID;

public class PreferencesUpdatedEvent {

    private UUID playerId;
    private Map<String, Object> newPreferences;

    public PreferencesUpdatedEvent() {
    }

    public PreferencesUpdatedEvent(UUID playerId, Map<String, Object> newPreferences) {
        this.playerId = playerId;
        this.newPreferences = newPreferences;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public Map<String, Object> getNewPreferences() {
        return newPreferences;
    }

    public void setNewPreferences(Map<String, Object> newPreferences) {
        this.newPreferences = newPreferences;
    }

}
