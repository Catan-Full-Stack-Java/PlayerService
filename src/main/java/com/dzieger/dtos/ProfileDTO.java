package com.dzieger.dtos;

import java.util.Map;

public class ProfileDTO {

    private int gamesPlayed;
    private int gamesWon;
    private int leaderboardPosition;
    private long timePlayed;
    private int wallet;
    private Map<String, Object> preferences;

    public ProfileDTO() {
    }

    public ProfileDTO(int gamesPlayed, int gamesWon, int leaderboardPosition, long timePlayed, int wallet, Map<String, Object> preferences) {
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.leaderboardPosition = leaderboardPosition;
        this.timePlayed = timePlayed;
        this.wallet = wallet;
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

    public int getWallet() {
        return wallet;
    }

    public void setWallet(int wallet) {
        this.wallet = wallet;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }
}
