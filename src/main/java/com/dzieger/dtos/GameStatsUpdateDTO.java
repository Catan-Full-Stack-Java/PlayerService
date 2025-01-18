package com.dzieger.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class GameStatsUpdateDTO {

    @NotNull(message = "Player ID cannot be null")
    private UUID playerId;

    @NotNull(message = "Win status cannot be null")
    private boolean win;

    public GameStatsUpdateDTO() {
    }

    public GameStatsUpdateDTO(UUID playerId, boolean win) {
        this.playerId = playerId;
        this.win = win;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

}
