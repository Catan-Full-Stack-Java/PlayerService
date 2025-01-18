package com.dzieger.models;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

// Added wallet with virtual coins and virtual currency
// Store service

@Component
@Entity
@Table(name = "player_profiles")
public class PlayerProfile {

    @Id
    @Column(name = "player_id", nullable = false, unique = true)
    private UUID playerId;

    // Player's preferences in JSON format
    private String preferences;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed = 0;

    @Column(name = "games_won", nullable = false)
    private int gamesWon = 0;

    @Column(name = "leaderboard_position")
    private int leaderboardPosition = 0;

    @Column(name = "time_played", nullable = false)
    private long timePlayed = 0;

    @Column(name = "wallet", nullable = false)
    private int wallet = 150;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public PlayerProfile() {
    }

    public PlayerProfile(UUID playerId, String preferences, int gamesPlayed, int gamesWon, int leaderboardPosition, long timePlayed, int wallet, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.playerId = playerId;
        this.preferences = preferences;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.leaderboardPosition = leaderboardPosition;
        this.timePlayed = timePlayed;
        this.wallet = wallet;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getLeaderboardPosition() {
        return leaderboardPosition;
    }

    public void setLeaderboardPosition(int leaderboardPosition) {
        this.leaderboardPosition = leaderboardPosition;
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public void setTimePlayed(long timePlayed) {
        this.timePlayed = timePlayed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getWallet() {
        return wallet;
    }

    public void setWallet(int wallet) {
        this.wallet = wallet;
    }
}
