package com.dzieger.services;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.dtos.PreferencesDTO;
import com.dzieger.dtos.ProfileDTO;
import com.dzieger.dtos.WalletDTO;
import com.dzieger.eventPayloadDtos.GameCompletedEvent;
import com.dzieger.eventPayloadDtos.LeaderboardUpdatedEvent;
import com.dzieger.exceptions.ProfileAlreadyExistsException;
import com.dzieger.models.PlayerProfile;
import com.dzieger.repositories.PlayerProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final JwtUtil jwtUtil;

    public PlayerProfileService(PlayerProfileRepository playerProfileRepository, JwtUtil jwtUtil) {
        this.playerProfileRepository = playerProfileRepository;
        this.jwtUtil = jwtUtil;
    }

    public String createProfile(UUID id){
        if(playerProfileRepository.findById(id).isPresent()) {
            throw new ProfileAlreadyExistsException("Profile already exists");
        }
        PlayerProfile newProfile = new PlayerProfile();
        newProfile.setPlayerId(id);
        newProfile.setPreferences(setDefaultPreferences());

        playerProfileRepository.save(newProfile);
        return "Profile created successfully";
    }

    public ProfileDTO getProfile(String token) {
        // get player profile

        return null;
    }

    public String deleteProfile(UUID playerId) {
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
        return null;
    }


    // Helper Methods

    private String setDefaultPreferences() {
        // set default preferences
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("notifications", true);
        preferences.put("sounds", true);
        preferences.put("music", true);
        preferences.put("default_game", "regular");

        return convertPreferencesToJson(preferences);
    }

    private boolean isValidPreference(String preference) {
        // check if preference is valid
        return false;
    }

    private String updatePreferences(Map<String, Object> preferences) throws JsonProcessingException {
        // update preferences
        return convertPreferencesToJson(preferences);
    }

    private String convertPreferencesToJson(Map<String, Object> preferences) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(preferences);
        } catch (JsonProcessingException) {
            throw new JsonProcessingException("Unable to convert Map to Json string");
        }
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