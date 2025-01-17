package com.dzieger.dtos;

import java.util.UUID;

public class LeaderboardUpdateDTO {

    private UUID playerId;
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
