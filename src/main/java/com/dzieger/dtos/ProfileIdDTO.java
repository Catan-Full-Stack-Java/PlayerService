package com.dzieger.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ProfileIdDTO {

    @NotNull(message = "Player ID cannot be null")
    private UUID playerId;

    public ProfileIdDTO(UUID playerId) {
        this.playerId = playerId;
    }

    public ProfileIdDTO() {
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }
}
