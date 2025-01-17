package com.dzieger.dtos;

import java.util.UUID;

public class GameStatsUpdateDTO {

    private UUID playerId;
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
