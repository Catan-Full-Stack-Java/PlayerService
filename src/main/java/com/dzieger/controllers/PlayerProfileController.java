package com.dzieger.controllers;

import com.dzieger.dtos.ProfileIdDTO;
import com.dzieger.dtos.PreferencesDTO;
import com.dzieger.dtos.ProfileDTO;
import com.dzieger.dtos.WalletDTO;
import com.dzieger.services.PlayerProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/player")
@Validated
@CrossOrigin
public class PlayerProfileController {

    private static final Logger log = LoggerFactory.getLogger(PlayerProfileController.class);

    private final PlayerProfileService playerProfileService;

    public PlayerProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @PostMapping("/v1/profile")
    public ResponseEntity<ApiResponse<String>> createProfile(@Valid @RequestBody ProfileIdDTO playerId) {
        log.info("Creating profile for player: {}", playerId.getPlayerId());
        String result = playerProfileService.createProfile(playerId.getPlayerId());
        log.info("Profile created successfully");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Profile created successfully", result));
    }


    @GetMapping("/v1/profile")
    public ResponseEntity<ApiResponse<ProfileDTO>> getProfile(@RequestHeader("Authorization") String token) {
        log.info("Retrieving profile for token: {}", token);
        ProfileDTO profile = playerProfileService.getProfile(token);
        log.info("Profile retrieved successfully: {}", profile);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", profile));
    }


    @DeleteMapping("/v1/profile")
    public ResponseEntity<ApiResponse<String>> deleteProfile(@Valid @RequestBody ProfileIdDTO playerId) {
        log.info("Deleting profile for player: {}", playerId.getPlayerId());
        String result = playerProfileService.deleteProfile(playerId.getPlayerId());
        log.info("Profile deleted successfully");
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile deleted successfully", result));
    }

    @GetMapping("/v1/profile/preferences")
    public ResponseEntity<ApiResponse<PreferencesDTO>> getPreferences(@RequestHeader("Authorization") String token) {
        PreferencesDTO preferences = playerProfileService.getProfilePreferences(token);
        log.info("Preferences: {}", preferences);
        return ResponseEntity.ok(new ApiResponse<>(true, "Preferences retrieved successfully", preferences));
    }


    @PatchMapping("/v1/profile/preferences")

    public ResponseEntity<?> updateProfilePreferences(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Preferences cannot be empty"));
        }
        PreferencesDTO updatedPreferences = playerProfileService.updateProfilePreferences(token, preferences);
        log.info("Updated preferences: {}", updatedPreferences);
        return ResponseEntity.ok(new ApiResponse<>(true, "Preferences updated successfully", updatedPreferences));
    }


    @GetMapping("/v1/profile/game-preferences")
    public ResponseEntity<ApiResponse<PreferencesDTO>> getGamePreferences(@RequestHeader("Authorization") String token) {
        PreferencesDTO gamePreferences = playerProfileService.getProfileGamePreferences(token);
        log.info("Game preferences: {}", gamePreferences);
        return ResponseEntity.ok(new ApiResponse<>(true, "Game preferences retrieved successfully", gamePreferences));
    }



//    @KafkaListener(topics = "game-completed", groupId = "player-service")
//    public void handleGameCompleted(GameCompletedEvent event) {
//        // process game completion
//        // add coins to wallet
//        playerProfileService.handleGameCompleted(event);
//    }
//
//    @KafkaListener(topics = "leaderbaord-updated", groupId = "player-service")
//    public void handleLeaderboardUpdated(LeaderboardUpdatedEvent event) {
//        // process leaderboard update
//        playerProfileService.handleLeaderboardUpdated(event);
//    }

    @PatchMapping("/v1/profile/wallet")
    public ResponseEntity<ApiResponse<WalletDTO>> updateWallet(@RequestHeader("Authorization") String token,@RequestBody WalletDTO wallet) {
        log.info("Updating wallet: {}, for token: {}", wallet, token);
        WalletDTO updatedWallet = playerProfileService.updateWallet(token, wallet);
        log.info("Updated wallet: {}", updatedWallet);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet updated successfully", updatedWallet));
    }


    @GetMapping("/v1/profile/wallet")
    public ResponseEntity<ApiResponse<WalletDTO>> getWallet(@RequestHeader("Authorization") String token) {
        WalletDTO wallet = playerProfileService.getWallet(token);
        log.info("Wallet: {}", wallet);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet retrieved successfully", wallet));
    }



}
