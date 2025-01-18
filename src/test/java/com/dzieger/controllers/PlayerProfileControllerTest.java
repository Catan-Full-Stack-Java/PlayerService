package com.dzieger.controllers;

import com.dzieger.dtos.*;
import com.dzieger.exceptions.GlobalExceptionHandler;
import com.dzieger.exceptions.ProfileAlreadyExistsException;
import com.dzieger.exceptions.ProfileNotFoundException;
import com.dzieger.services.PlayerProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
class PlayerProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlayerProfileService playerProfileService;

    @InjectMocks
    private PlayerProfileController playerProfileController;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(messageSource.getMessage(any(), any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArgument(1);
            return args != null && args.length > 0 ? args[0].toString() : "Default validation error message";
        });

        mockMvc = MockMvcBuilders.standaloneSetup(playerProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateProfile_ShouldReturnCreatedStatus() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(playerProfileService.createProfile(playerId)).thenReturn("Profile created successfully");

        mockMvc.perform(post("/api/v1/player/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\": \"" + playerId + "\"}"))
                .andExpect(status().isCreated())
                .andExpectAll(
                    jsonPath("$.success").value(true),
                    jsonPath("$.message").value("Profile created successfully")
                )
                .andDo(print());
    }

    @Test
    void testCreateProfile_ShouldReturnBadRequest_whenPlayerIdIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/player/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.playerId").value("Player ID cannot be null"))
                .andDo(print());
    }

    @Test
    void testCreateProfile_shouldReturnConflict_whenProfileAlreadyExists() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(playerProfileService.createProfile(playerId)).thenThrow(new ProfileAlreadyExistsException("Profile already exists"));

        mockMvc.perform(post("/api/v1/player/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\": \"" + playerId + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Profile already exists"))
                .andDo(print());
    }


    @Test
    void testGetProfile_ShouldReturnProfile() throws Exception {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setPreferences(Map.of(
                "music", true,
                "sounds", true,
                "default_game", "regular",
                "notifications", true
        ));
        profileDTO.setWallet(0);
        profileDTO.setTimePlayed(0);
        profileDTO.setLeaderboardPosition(0);
        profileDTO.setGamesWon(2);
        profileDTO.setGamesPlayed(10);
        when(playerProfileService.getProfile(anyString())).thenReturn(profileDTO);

        mockMvc.perform(get("/api/v1/player/v1/profile")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.success").value(true),
                        jsonPath("$.data.gamesPlayed").value(10)
                )
                .andDo(print());
    }

    @Test
    void testDeleteProfile_ShouldReturnSuccess() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(playerProfileService.deleteProfile(any(UUID.class))).thenReturn("Profile deleted successfully");

        mockMvc.perform(delete("/api/v1/player/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\": \"" + playerId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile deleted successfully"))
                .andDo(print());
    }

    @Test
    void testDeleteProfile_shouldReturnNoContent_whenNoProfileFound() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(playerProfileService.deleteProfile(any(UUID.class))).thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(delete("/api/v1/player/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\": \"" + playerId + "\"}"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Profile not found"))
                .andDo(print());
    }

    @Test
    void testGetPreferences_ShouldReturnPreferences() throws Exception {
        PreferencesDTO preferencesDTO = new PreferencesDTO(Map.of(
                "music", true,
                "sounds", true,
                "default_game", "regular",
                "notifications", true
        ));
        when(playerProfileService.getProfilePreferences(anyString())).thenReturn(preferencesDTO);

        System.out.println(preferencesDTO.getPreferences());

        mockMvc.perform(get("/api/v1/player/v1/profile/preferences")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.preferences.music").value(true))
                .andExpect(jsonPath("$.data.preferences.sounds").value(true))
                .andExpect(jsonPath("$.data.preferences.default_game").value("regular"))
                .andExpect(jsonPath("$.data.preferences.notifications").value(true))
                .andDo(print());
    }

    @Test
    void testGetPreferences_shouldReturnNoContent_whenNoProfileFound() throws Exception {
        when(playerProfileService.getProfilePreferences(anyString())).thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(get("/api/v1/player/v1/profile/preferences")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Profile not found"))
                .andDo(print());
    }

    @Test
    void testUpdatePreferences_ShouldReturnUpdatedPreferences() throws Exception {
        PreferencesDTO updatedPreferences = new PreferencesDTO(Map.of("notifications", false));
        when(playerProfileService.updateProfilePreferences(anyString(), any(Map.class))).thenReturn(updatedPreferences);

        mockMvc.perform(patch("/api/v1/player/v1/profile/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content("{\"notifications\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.preferences.notifications").value(false))
                .andDo(print());
    }

    @Test
    void testUpdatePreferences_shouldReturnBadRequest_whenPreferencesAreEmpty() throws Exception {
        mockMvc.perform(patch("/api/v1/player/v1/profile/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Preferences cannot be empty"))
                .andDo(print());
    }

    @Test
    void testUpdatePreferences_shouldReturnNoContent_whenNoProfileFound() throws Exception {
        when(playerProfileService.updateProfilePreferences(anyString(), any(Map.class))).thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(patch("/api/v1/player/v1/profile/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content("{\"notifications\": false}"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Profile not found"))
                .andDo(print());
    }

    @Test
    void testUpdateWallet_ShouldReturnUpdatedWallet() throws Exception {
        WalletDTO updatedWallet = new WalletDTO(200, 50);
        when(playerProfileService.updateWallet(anyString(), any(WalletDTO.class))).thenReturn(updatedWallet);

        mockMvc.perform(patch("/api/v1/player/v1/profile/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content("{\"balance\": 200, \"changeAmount\": 50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(200))
                .andExpect(jsonPath("$.data.changeAmount").value(50))
                .andDo(print());
    }

    @Test
    void testGetWallet_ShouldReturnWallet() throws Exception {
        WalletDTO walletDTO = new WalletDTO(150, 0);
        when(playerProfileService.getWallet(anyString())).thenReturn(walletDTO);

        mockMvc.perform(get("/api/v1/player/v1/profile/wallet")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(150))
                .andDo(print());
    }
}
