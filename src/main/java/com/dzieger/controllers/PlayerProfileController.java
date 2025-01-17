package com.dzieger.controllers;

import com.dzieger.dtos.ProfileIdDTO;
import com.dzieger.dtos.PreferencesDTO;
import com.dzieger.dtos.ProfileDTO;
import com.dzieger.dtos.WalletDTO;
import com.dzieger.eventPayloadDtos.GameCompletedEvent;
import com.dzieger.eventPayloadDtos.LeaderboardUpdatedEvent;
import com.dzieger.services.PlayerProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/player")
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;

    public PlayerProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @PostMapping("/v1/profile")
    public ResponseEntity<ApiResponse<String>> createProfile(@RequestBody ProfileIdDTO playerId) {
        String result = playerProfileService.createProfile(playerId.getPlayerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Profile created successfully", result));
    }


    @GetMapping("/v1/profile")
    public ResponseEntity<ApiResponse<ProfileDTO>> getProfile(@RequestHeader("Authorization") String token) {
        ProfileDTO profile = playerProfileService.getProfile(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", profile));
    }


    @DeleteMapping("/v1/profile")
    public ResponseEntity<ApiResponse<String>> deleteProfile(@RequestBody ProfileIdDTO playerId) {
        String result = playerProfileService.deleteProfile(playerId.getPlayerId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile deleted successfully", result));
    }

    @GetMapping("/v1/profile/preferences")
    public ResponseEntity<ApiResponse<PreferencesDTO>> getPreferences(@RequestHeader("Authorization") String token) {
        PreferencesDTO preferences = playerProfileService.getProfilePreferences(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Preferences retrieved successfully", preferences));
    }


    @PutMapping("/v1/profile/preferences")
    public ResponseEntity<ApiResponse<PreferencesDTO>> updatePreferences(
            @RequestHeader("Authorization") String token, @RequestBody Map<String, Object> preferences) {
        PreferencesDTO updatedPreferences = playerProfileService.updateProfilePreferences(token, preferences);
        return ResponseEntity.ok(new ApiResponse<>(true, "Preferences updated successfully", updatedPreferences));
    }


    @GetMapping("/v1/profile/game-preferences")
    public ResponseEntity<ApiResponse<PreferencesDTO>> getGamePreferences(@RequestHeader("Authorization") String token) {
        PreferencesDTO gamePreferences = playerProfileService.getProfileGamePreferences(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Game preferences retrieved successfully", gamePreferences));
    }



    @KafkaListener(topics = "game-completed", groupId = "player-service")
    public void handleGameCompleted(GameCompletedEvent event) {
        // process game completion
        // add coins to wallet
        playerProfileService.handleGameCompleted(event);
    }

    @KafkaListener(topics = "leaderbaord-updated", groupId = "player-service")
    public void handleLeaderboardUpdated(LeaderboardUpdatedEvent event) {
        // process leaderboard update
        playerProfileService.handleLeaderboardUpdated(event);
    }

    @PatchMapping("/v1/profiles/wallet")
    public ResponseEntity<ApiResponse<WalletDTO>> updateWallet(@RequestHeader("Authorization") String token, @RequestBody WalletDTO wallet) {
        WalletDTO updatedWallet = playerProfileService.updateWallet(token, wallet);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet updated successfully", updatedWallet));
    }


    @GetMapping("/v1/profiles/wallet")
    public ResponseEntity<ApiResponse<WalletDTO>> getWallet(@RequestHeader("Authorization") String token) {
        WalletDTO wallet = playerProfileService.getWallet(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet retrieved successfully", wallet));
    }



}
