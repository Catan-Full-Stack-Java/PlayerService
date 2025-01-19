package com.dzieger.services;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.dtos.PreferencesDTO;
import com.dzieger.dtos.ProfileDTO;
import com.dzieger.dtos.WalletDTO;
import com.dzieger.eventPayloadDtos.GameCompletedEvent;
import com.dzieger.eventPayloadDtos.LeaderboardUpdatedEvent;
import com.dzieger.exceptions.*;
import com.dzieger.models.PlayerProfile;
import com.dzieger.repositories.PlayerProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PlayerProfileService {

    private static final Logger log = LoggerFactory.getLogger(PlayerProfileService.class);

    private final PlayerProfileRepository playerProfileRepository;
    private final JwtUtil jwtUtil;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Set<Object> VALID_PREFERENCES = Set.of("notifications", "sounds", "music", "default_game", "num_of_players", "language");
    private static final Set<Object> GAME_PREFERENCES = Set.of("default_game", "num_of_players", "language");

    public PlayerProfileService(PlayerProfileRepository playerProfileRepository, JwtUtil jwtUtil, KafkaTemplate<String, String> kafkaTemplate) {
        this.playerProfileRepository = playerProfileRepository;
        this.jwtUtil = jwtUtil;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String createProfile(String token){
        log.info("Creating profile for player with id: {}", jwtUtil.extractUserId(token.substring(7)));

        UUID id = UUID.fromString(jwtUtil.extractUserId(token.substring(7)));

        if(playerProfileRepository.findById(id).isPresent()) {
            throw new ProfileAlreadyExistsException("Profile already exists");
        }

        PlayerProfile newProfile = new PlayerProfile();
        newProfile.setPlayerId(id);
        newProfile.setPreferences(setDefaultPreferences());
        newProfile.setGamesPlayed(0);
        newProfile.setGamesWon(0);
        newProfile.setLeaderboardPosition(0);
        newProfile.setTimePlayed(0);
        newProfile.setWallet(150);

        playerProfileRepository.save(newProfile);
        log.info("Profile created successfully for player with id: {}", id);
        return "Profile created successfully";
    }

    // TODO: Create profile with default preferences if profile does not exist when accessing getProfile endpoint
    public ProfileDTO getProfile(String token) {
        log.info("Getting profile for player");
        // get player profile
        UUID playerId = UUID.fromString(jwtUtil.extractUserId(token.substring(7)));
        PlayerProfile playerProfile = playerProfileRepository.findById(playerId).orElseThrow(() ->
                new ProfileNotFoundException("Profile not found"));

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setPreferences(convertJsonToMappedPreferences(playerProfile.getPreferences()));
        profileDTO.setGamesPlayed(playerProfile.getGamesPlayed());
        profileDTO.setGamesWon(playerProfile.getGamesWon());
        profileDTO.setLeaderboardPosition(playerProfile.getLeaderboardPosition());
        profileDTO.setTimePlayed(playerProfile.getTimePlayed());
        profileDTO.setWallet(playerProfile.getWallet());

        log.info("Profile: {}", profileDTO);

        return profileDTO;
    }

    public String deleteProfile(UUID playerId) {
        log.info("Deleting profile for player with id: {}", playerId);

        // delete player profile
        if (playerProfileRepository.findById(playerId).isEmpty()) {
            throw new ProfileNotFoundException("Profile not found");
        }
        playerProfileRepository.deleteById(playerId);
        return "Player profile deleted successfully";
    }

    public PreferencesDTO getProfilePreferences(String token) {
        log.info("Getting preferences for player");

        // get player preferences
        UUID playerId = UUID.fromString(jwtUtil.extractUserId(token.substring(7)));

        PlayerProfile playerProfile = playerProfileRepository.findById(playerId).orElseThrow(() ->
                new ProfileNotFoundException("Profile not found"));


        return new PreferencesDTO(convertJsonToMappedPreferences(playerProfile.getPreferences()));
    }

    public PreferencesDTO updateProfilePreferences(String token, Map<String, Object> preferences) {

        if (preferences == null || preferences.isEmpty()) {
            throw new InvalidPreferenceException("Preferences cannot be null");
        }

        log.info("Updating preferences for player: {}", preferences);

        // update player preferences
        UUID playerId = UUID.fromString(jwtUtil.extractUserId(token.substring(7)));
        PlayerProfile playerProfile = playerProfileRepository.findById(playerId).orElseThrow(() ->
                new ProfileNotFoundException("Profile not found"));
        playerProfile.setPreferences(updatePreferences(playerProfile, preferences));
        playerProfileRepository.save(playerProfile);

        return new PreferencesDTO(preferences);
    }

    public void handleGameCompleted(GameCompletedEvent event) {
        log.info("Handling game completion event");

        // process game completion
        PlayerProfile profile = playerProfileRepository.findById(event.getPlayerId()).orElseThrow(() ->
                new ProfileNotFoundException("Profile not found"));
        profile.setGamesPlayed(profile.getGamesPlayed() + 1);
        if (event.isWon()) {
            profile.setGamesWon(profile.getGamesWon() + 1);
        }
        playerProfileRepository.save(profile);

        // produce stats for leaderboard service
        double stats = (double) profile.getGamesWon() / profile.getGamesPlayed();

        // produce stats for leaderboard service
        String eventPayload = String.format("{\"playerId\": \"%s\", \"stats\": %f}", profile.getPlayerId(), stats);

        try {
            kafkaTemplate.send("leaderboard-stats", eventPayload);
        } catch (Exception e) {
            log.error("Failed to send leaderboard stats to Kafka", e);
        }

    }

    public void handleLeaderboardUpdated(LeaderboardUpdatedEvent event) {
        log.info("Handling leaderboard update event");

        // update leaderboard position with received position
        PlayerProfile profile = playerProfileRepository.findById(event.getPlayerId()).orElseThrow(() ->
                new ProfileNotFoundException("Profile not found"));
        profile.setLeaderboardPosition(event.getNewLeaderboardPosition());
        playerProfileRepository.save(profile);
    }

    public WalletDTO updateWallet(String token, WalletDTO wallet) {
        // update wallet
        PlayerProfile profile = playerProfileRepository.findById(UUID.fromString(jwtUtil.extractUserId(token.substring(7))))
                .orElseThrow(() ->
                new ProfileNotFoundException("Profile not found"));

        if (profile.getWallet() + wallet.getChangeAmount() < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        profile.setWallet(profile.getWallet() + wallet.getChangeAmount());

        playerProfileRepository.save(profile);

        WalletDTO updatedWallet = new WalletDTO();
        updatedWallet.setBalance(profile.getWallet());
        updatedWallet.setChangeAmount(wallet.getChangeAmount());

        return updatedWallet;
    }

    public WalletDTO getWallet(String token) {
        // get wallet
        PlayerProfile profile = playerProfileRepository.findById(UUID.fromString(jwtUtil.extractUserId(token.substring(7)))).orElseThrow(() ->
                new ProfileNotFoundException("Profile not found"));
        WalletDTO wallet = new WalletDTO();
        wallet.setBalance(profile.getWallet());
        return wallet;
    }


    // Helper Methods

    private String setDefaultPreferences() {
        // set default preferences
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("notifications", true);
        preferences.put("sounds", true);
        preferences.put("music", true);
        preferences.put("default_game", "regular");

        return convertMappedPreferencesToJson(preferences);
    }

    private boolean isValidPreference(String preference) {
        // check if preference is valid
        return VALID_PREFERENCES.contains(preference);
    }

    private String updatePreferences(PlayerProfile profile, Map<String, Object> preferences) {
        // update preferences
        String preferencesJson = profile.getPreferences();
        if (preferencesJson == null || preferencesJson.isEmpty()) {
            log.warn("Profile has no existing preferences. Initializing empty preferences.");
            preferencesJson = "{}";
        }
        Map<String, Object> profilePreferences = convertJsonToMappedPreferences(preferencesJson);
        log.info("Updating preferences: {}", preferences);
        preferences.forEach((key, value) -> {
            log.info("Updating preference: {} with value: {}", key, value);
            if (isValidPreference(key)) {
                profilePreferences.put(key, value);
            } else {
                throw new InvalidPreferenceException("Invalid preference: " + key);
            }
        });

        return convertMappedPreferencesToJson(profilePreferences);
    }

    private String convertMappedPreferencesToJson(Map<String, Object> preferences) {
        log.info("Converting preferences to Json string");
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(preferences);
        } catch (JsonProcessingException e) {
            throw new FailedConversionToJsonException("Unable to convert Map to Json string", e);
        }
    }

    private Map<String, Object> convertJsonToMappedPreferences(String preferences) {
        try {
            log.info("Converting Json string to Map: {}", preferences);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(preferences, Map.class);
        } catch (JsonProcessingException e) {
            throw new FailedConversionToJsonException("Unable to convert Json string to Map", e);
        }
    }

    public PreferencesDTO getProfileGamePreferences(String token) {
        // get player game preferences
        UUID playerId = UUID.fromString(jwtUtil.extractUserId(token.substring(7)));
        PlayerProfile profile = playerProfileRepository.findById(playerId).orElseThrow(() ->
                new ProfileNotFoundException("Profile not found"));

        Map<String, Object> gamePreferences = new HashMap<>();
        Map<String, Object> preferences = convertJsonToMappedPreferences(profile.getPreferences());

        preferences.forEach((key, value) -> {
            if (GAME_PREFERENCES.contains(key)) {
                gamePreferences.put(key, value);
            }
        });

        return new PreferencesDTO(gamePreferences);
    }

}