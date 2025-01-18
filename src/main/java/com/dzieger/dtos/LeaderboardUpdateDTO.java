package com.dzieger.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class LeaderboardUpdateDTO {

    @NotNull(message = "Player ID cannot be null")
    private UUID playerId;

    @NotNull(message = "Player's new position cannot be null")
    private int newPosition;

    public LeaderboardUpdateDTO() {
    }

    public LeaderboardUpdateDTO(UUID playerId, int newPosition) {
        this.playerId = playerId;
        this.newPosition = newPosition;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public int getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(int newPosition) {
        this.newPosition = newPosition;
    }

}
