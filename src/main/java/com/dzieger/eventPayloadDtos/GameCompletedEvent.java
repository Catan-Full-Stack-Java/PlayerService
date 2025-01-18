package com.dzieger.eventPayloadDtos;

import java.util.UUID;

public class GameCompletedEvent {

    private UUID playerId;
    private boolean won;

    public GameCompletedEvent() {
    }

    public GameCompletedEvent(UUID playerId, boolean won) {
        this.playerId = playerId;
        this.won = won;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

}
