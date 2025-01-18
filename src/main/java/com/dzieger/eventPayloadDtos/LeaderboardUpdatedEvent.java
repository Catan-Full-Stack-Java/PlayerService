package com.dzieger.eventPayloadDtos;

import java.util.UUID;

public class LeaderboardUpdatedEvent {

    private UUID playerId;
    private int newLeaderboardPosition;

    public LeaderboardUpdatedEvent() {
    }

    public LeaderboardUpdatedEvent(UUID playerId, int newLeaderboardPosition) {
        this.playerId = playerId;
        this.newLeaderboardPosition = newLeaderboardPosition;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public int getNewLeaderboardPosition() {
        return newLeaderboardPosition;
    }

    public void setNewLeaderboardPosition(int newLeaderboardPosition) {
        this.newLeaderboardPosition = newLeaderboardPosition;
    }

}
