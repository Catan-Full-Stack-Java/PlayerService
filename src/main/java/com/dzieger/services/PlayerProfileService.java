package com.dzieger.services;

import com.dzieger.dtos.PreferencesDTO;
import com.dzieger.dtos.ProfileDTO;
import com.dzieger.dtos.WalletDTO;
import com.dzieger.eventPayloadDtos.GameCompletedEvent;
import com.dzieger.eventPayloadDtos.LeaderboardUpdatedEvent;
import com.dzieger.repositories.PlayerProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;

    public PlayerProfileService(PlayerProfileRepository playerProfileRepository) {
        this.playerProfileRepository = playerProfileRepository;
    }

    public String createProfile(String token) {
        // create player profile
        // Set default preferences
        setDefaultPreferences();
        return null;
    }

    public ProfileDTO getProfile(String token) {
        // get player profile
        return null;
    }

    public String deleteProfile(String token) {
        // delete player profile
        return null;
    }

    public PreferencesDTO getProfilePreferences(String token) {
        // get player preferences
        return null;
    }

    public PreferencesDTO updateProfilePreferences(String token, Map<String, Object> preferences) {
        // update player preferences
        return null;
    }

    public void handleGameCompleted(GameCompletedEvent event) {
        // process game completion
        // increase games played and maybe games won
        // produce stats for leaderboard service
    }

    public void handleLeaderboardUpdated(LeaderboardUpdatedEvent event) {
        // update leaderboard position with received position
    }

    public WalletDTO updateWallet(String token, WalletDTO wallet) {
        // update wallet
        return null;
    }

    public WalletDTO getWallet(String token) {
        // get wallet
    }


    // Helper Methods

    private String setDefaultPreferences() {
        // set default preferences
        return null;
    }

    private boolean isValidPreference(String preference) {
        // check if preference is valid
        return false;
    }

    private String updatePreferences(Map<String, Object> preferences) {
        // update preferences
        return convertPreferencesToJson(preferences);
    }

    private String convertPreferencesToJson(Map<String, Object> preferences) {
        return null;
    }

    public PreferencesDTO getProfileGamePreferences(String token) {
        return null;
    }

    /*
    * Pick a character
    * Game style (Regualar, Sea Fareres) {default settings for creating a game}
    * Num of People to play with {default settings for creating a game}
    * Language {Default settings for creating a game}
    * Notification Preferences
    * Visual Themes {Background, table board sits on}
    * Visual Themes {Game pieces, Dice}
    * Sounds settings
    *
    *
    * */
}