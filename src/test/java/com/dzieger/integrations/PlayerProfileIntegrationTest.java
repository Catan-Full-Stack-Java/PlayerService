package com.dzieger.integrations;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.exceptions.FailedConversionToJsonException;
import com.dzieger.exceptions.InsufficientFundsException;
import com.dzieger.exceptions.ProfileAlreadyExistsException;
import com.dzieger.models.PlayerProfile;
import com.dzieger.models.enums.Role;
import com.dzieger.repositories.PlayerProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class PlayerProfileIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerProfileRepository playerProfileRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeAll
    public void setup() {
        playerProfileRepository.deleteAll();
    }

    @BeforeEach
    void cleanUp() {
        playerProfileRepository.deleteAll();
    }



    @Test
    void testCreateProfile_createsNewProfileSuccessfully() throws Exception {
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        String requestBody = """
                {
                    "playerId": "%s"
                }
                """.formatted(playerId);



        mockMvc.perform(post("/api/v1/player/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        Optional<PlayerProfile> profile = playerProfileRepository.findById(playerId);
        assertTrue(profile.isPresent());
        assertEquals(playerId, profile.get().getPlayerId());
        assertFalse(profile.get().getPreferences().isEmpty());
        assertEquals(150, profile.get().getWallet());
        assertEquals(0, profile.get().getGamesPlayed());
        assertEquals(0, profile.get().getGamesWon());
    }

    @Test
    void testCreateProfile_returnsConflict_whenProfileAlreadyExists() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles).trim();

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Manually create and save the profile
        PlayerProfile existingProfile = new PlayerProfile();
        existingProfile.setPlayerId(playerId);
        existingProfile.setPreferences("{}"); // Default empty preferences
        existingProfile.setWallet(150);
        existingProfile.setGamesPlayed(0);
        existingProfile.setGamesWon(0);
        playerProfileRepository.save(existingProfile);

        // Act & Assert
        mockMvc.perform(post("/api/v1/player/v1/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ProfileAlreadyExistsException))
                .andExpect(result -> assertEquals("Profile already exists", result.getResolvedException().getMessage()));
    }


    @Test
    void testGetProfile_returnProfileDTO() throws Exception {
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Create the profile
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        profile.setPreferences("{}"); // Default empty preferences
        profile.setWallet(150); // Set default wallet for completeness
        profile.setGamesPlayed(0);
        profile.setGamesWon(0);
        playerProfileRepository.save(profile);

        mockMvc.perform(get("/api/v1/player/v1/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPreferences_returnsPreferencesDTO() throws Exception{
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        String requestBody = """
                {
                    "playerId": "%s"
                }
                """.formatted(playerId);

        mockMvc.perform(post("/api/v1/player/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/player/v1/profile/preferences")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePreferences_withValidPreferences() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Create the profile
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        profile.setPreferences("{}"); // Default empty preferences
        profile.setWallet(150); // Set default wallet for completeness
        profile.setGamesPlayed(0);
        profile.setGamesWon(0);
        playerProfileRepository.save(profile);

        // Act - Update the preferences
        String updateRequestBody = """
            {
                "default_game": "regular",
                "sounds": false
            }
            """;

        mockMvc.perform(patch("/api/v1/player/v1/profile/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(updateRequestBody))
                .andExpect(status().isOk());

        // Assert - Verify the preferences are updated
        Optional<PlayerProfile> updatedProfile = playerProfileRepository.findById(playerId);
        assertTrue(updatedProfile.isPresent());

        Map<String, Object> updatedPreferences = convertJsonToMappedPreferences(updatedProfile.get().getPreferences());
        assertEquals("regular", updatedPreferences.get("default_game"));
        assertEquals(false, updatedPreferences.get("sounds"));
    }


    @Test
    void testUpdatePreferences_returnsBadRequest_whenPreferencesAreEmpty() throws Exception {
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Create the profile
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        profile.setPreferences("{}"); // Default empty preferences
        profile.setWallet(150); // Set default wallet for completeness
        profile.setGamesPlayed(0);
        profile.setGamesWon(0);
        playerProfileRepository.save(profile);

        String updateRequestBody = "{}";

        mockMvc.perform(patch("/api/v1/player/v1/profile/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(updateRequestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePreferences_returnsBadRequest_whenPreferencesDoNotExist() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Create the profile
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        profile.setPreferences("{}"); // No preferences initially
        playerProfileRepository.save(profile);

        // Act - Attempt to update invalid preferences
        String updateRequestBody = """
        {
            "default": "regular",
            "sounds": false
        }
        """;

        // Assert - Verify bad request for invalid preferences
        mockMvc.perform(patch("/api/v1/player/v1/profile/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(updateRequestBody))
                .andExpect(status().isBadRequest());
    }



    @Test
    void testDeleteProfile_deletesProfileSuccessfully() throws Exception {
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        String requestBody = """
                {
                    "playerId": "%s"
                }
                """.formatted(playerId);

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Create the profile
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        profile.setPreferences("{}"); // Default empty preferences
        profile.setWallet(150); // Set default wallet for completeness
        profile.setGamesPlayed(0);
        profile.setGamesWon(0);
        playerProfileRepository.save(profile);

        mockMvc.perform(delete("/api/v1/player/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());

        assertFalse(playerProfileRepository.findById(playerId).isPresent());
    }

    @Test
    void testDeleteProfile_returnsNotFound_whenProfileDoesNotExist() throws Exception {
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        String requestBody = """
                {
                    "playerId": "%s"
                }
                """.formatted(playerId);

        mockMvc.perform(delete("/api/v1/player/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetWallet_returnsWalletAmount() throws Exception {
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Create the profile
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        profile.setPreferences("{}"); // Default empty preferences
        profile.setWallet(150); // Set default wallet for completeness
        profile.setGamesPlayed(0);
        profile.setGamesWon(0);
        playerProfileRepository.save(profile);

        mockMvc.perform(get("/api/v1/player/v1/profile/wallet")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateWallet_updatesWalletAmount_positiveNumber() throws Exception {
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        String requestBodyCreate = """
                {
                    "playerId": "%s"
                }
                """.formatted(playerId);

        mockMvc.perform(post("/api/v1/player/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBodyCreate))
                .andExpect(status().isCreated());

        Optional<PlayerProfile> profile = playerProfileRepository.findById(playerId);
        assertTrue(profile.isPresent());
        assertEquals(150, profile.get().getWallet());

        String requestBodyUpdateWallet = """
                {
                    "changeAmount": %d
                }
                """.formatted( 50);

        mockMvc.perform(patch("/api/v1/player/v1/profile/wallet")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBodyUpdateWallet))
                .andExpect(status().isOk());

        Optional<PlayerProfile> updatedProfile = playerProfileRepository.findById(playerId);
        assertTrue(updatedProfile.isPresent());
        assertEquals(200, updatedProfile.get().getWallet());
    }

    @Test
    void testUpdateWallet_updatesWalletAmount_negativeNumber() throws Exception {
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Create the profile
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        profile.setPreferences("{}"); // Default empty preferences
        profile.setWallet(150); // Set default wallet for completeness
        profile.setGamesPlayed(0);
        profile.setGamesWon(0);
        playerProfileRepository.save(profile);

        String requestBodyUpdateWallet = """
                {
                    "changeAmount": %d
                }
                """.formatted( -50);

        mockMvc.perform(patch("/api/v1/player/v1/profile/wallet")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBodyUpdateWallet))
                .andExpect(status().isOk());

        Optional<PlayerProfile> updatedProfile = playerProfileRepository.findById(playerId);
        assertTrue(updatedProfile.isPresent());
        assertEquals(100, updatedProfile.get().getWallet());
    }

    @Test
    void testUpdateWallet_returnsBadRequest_whenBalanceBecomesNegative() throws Exception {
        // Given
        UUID playerId = UUID.randomUUID();
        String username = "testUser";
        List<Role> roles = List.of(Role.PLAYER);
        String token = jwtUtil.generateToken(playerId.toString(), username, roles);

        // Ensure the database is clean
        playerProfileRepository.deleteAll();

        // Create the profile
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);
        profile.setPreferences("{}"); // Default empty preferences
        profile.setWallet(150); // Set default wallet for completeness
        profile.setGamesPlayed(0);
        profile.setGamesWon(0);
        playerProfileRepository.save(profile);

        String requestBodyUpdateWallet = """
                {
                    "changeAmount": %d
                }
                """.formatted( -200);

            mockMvc.perform(patch("/api/v1/player/v1/profile/wallet")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(requestBodyUpdateWallet))
                    .andExpect(status().isBadRequest());
    }


    // Helper methods
    private String convertMappedPreferencesToJson(Map<String, Object> preferences) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(preferences);
        } catch (JsonProcessingException e) {
            throw new FailedConversionToJsonException("Unable to convert Map to Json string", e);
        }
    }

    private Map<String, Object> convertJsonToMappedPreferences(String preferences) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(preferences, Map.class);
        } catch (JsonProcessingException e) {
            throw new FailedConversionToJsonException("Unable to convert Json string to Map", e);
        }
    }

}
