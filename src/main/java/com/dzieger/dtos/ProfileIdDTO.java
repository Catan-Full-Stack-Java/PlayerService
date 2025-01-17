package com.dzieger.dtos;

import java.util.UUID;

public class ProfileIdDTO {

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
