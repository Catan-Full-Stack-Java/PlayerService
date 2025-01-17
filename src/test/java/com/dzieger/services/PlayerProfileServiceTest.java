package com.dzieger.services;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.config.Parameters;
import com.dzieger.dtos.PreferencesDTO;
import com.dzieger.dtos.ProfileDTO;
import com.dzieger.dtos.WalletDTO;
import com.dzieger.eventPayloadDtos.GameCompletedEvent;
import com.dzieger.eventPayloadDtos.LeaderboardUpdatedEvent;
import com.dzieger.exceptions.NegativeBalanceException;
import com.dzieger.exceptions.ProfileAlreadyExistsException;
import com.dzieger.exceptions.ProfileNotFoundException;
import com.dzieger.models.PlayerProfile;
import com.dzieger.models.enums.Role;
import com.dzieger.repositories.PlayerProfileRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.security.Key;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PlayerProfileServiceTest {

    @Mock
    private PlayerProfileRepository playerProfileRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private Parameters params;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private PlayerProfileService playerProfileService;

    private static final UUID playerId = UUID.randomUUID();
    private static final String USERNAME = "testUser";
    private static final Role role = Role.PLAYER;
    private static final List<String> roles = List.of(role.toString());

    private Key secretKey;
    private final String jwtIssuer = "testIssuer";
    private final long jwtExpiration = 3600000;
    private static String token;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String jwtSecret = "thisisaverysecuresecretkeyforsigningjwt123";
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        when(params.getJwtSecret()).thenReturn(jwtSecret);
        when(params.getJwtIssuer()).thenReturn(jwtIssuer);
        when(params.getJwtExpiration()).thenReturn(String.valueOf(jwtExpiration));

        token = generateToken();
    }

    // Helper method to generate a token
    private String generateToken() {
        return Jwts.builder()
                .setSubject(playerId.toString())
                .claim("username", USERNAME)
                .claim("roles", roles)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
    }

    @Test
    void testCreateProfile_ShouldCreateProfileWithDefaultPreferences() {
        UUID newplayer = UUID.randomUUID();

        when(playerProfileRepository.findById(newplayer)).thenReturn(Optional.empty());

        String result = playerProfileService.createProfile(newplayer);

        assertNotNull(result);
        verify(playerProfileRepository, times(1)).save(any(PlayerProfile.class));
    }

    @Test
    void testCreateProfile_ProfileExists_ShouldThrowError() {
        UUID newPlayer = UUID.randomUUID();

        when(playerProfileRepository.existsById(playerId)).thenReturn(true);

        assertThrows(ProfileAlreadyExistsException.class, () -> {
            playerProfileService.createProfile(newPlayer);
        });
        verify(playerProfileRepository, times(0)).save(any(PlayerProfile.class));
    }

    @Test
    void testGetProfile_ShouldReturnProfile() {
        UUID existingPlayerId = UUID.fromString(jwtUtil.extractUserId(token));
        PlayerProfile playerProfile = new PlayerProfile();
        playerProfile.setPlayerId(playerId);

        when(playerProfileRepository.findById(existingPlayerId)).thenReturn(Optional.of(playerProfile));

        ProfileDTO result = playerProfileService.getProfile(token);

        assertNotNull(result);
    }

    @Test
    void testGetProfile_WithInvalidToken_ShouldThrowError() {
        when(jwtUtil.extractUserId("invalidToken")).thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class, () -> {
            playerProfileService.getProfile("invalidToken");
        });
    }


    @Test
    void testUpdateProfilePreferences_shouldUpdatePreferences() {
        UUID existingPlayerId = UUID.fromString(jwtUtil.extractUserId(token));
        Map<String, Object> newPreferences = Map.of("notifications", true);

        PreferencesDTO updatedPreferences = new PreferencesDTO();
        updatedPreferences.setPreferences(newPreferences);

        when(playerProfileRepository.findById(existingPlayerId)).thenReturn(Optional.of(new PlayerProfile()));
        when(playerProfileRepository.save(any(PlayerProfile.class))).thenReturn(new PlayerProfile());

        PreferencesDTO result = playerProfileService.updateProfilePreferences(token, newPreferences);

        assertNotNull(result);
        assertThat(result.getPreferences()).containsEntry("notifications", true);
    }

    @Test
    void testUpdateProfilePreferences_whenProfileDoesNotExtist_ShouldThrowError() {
        UUID existingPlayerId = UUID.fromString(jwtUtil.extractUserId(token));
        Map<String, Object> preferences = Map.of("notifications", true);

        when(playerProfileRepository.findById(existingPlayerId)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> {
            playerProfileService.updateProfilePreferences(token, preferences);
        });

        verify(playerProfileRepository, times(0)).save(any(PlayerProfile.class));
    }

    @Test
    void testUpdateProfilePreferences_WithNullPreferences_ShouldThrowError() {
        assertThrows(IllegalArgumentException.class, () -> {
            playerProfileService.updateProfilePreferences(token, null);
        });
    }



    @Test
    void testDeleteProfile_WhenProfileExists_ShouldDeleteProfile() {
        UUID newPlayer = UUID.fromString(jwtUtil.extractUserId(token));
        PlayerProfile profile = new PlayerProfile();

        when(playerProfileRepository.findById(newPlayer)).thenReturn(Optional.of(profile));

        String result = playerProfileService.deleteProfile(newPlayer);

        assertNotNull(result);
    }

    @Test
    void testDeleteProfile_WhenProfileDoesNotExist_ShouldThrowError() {
        UUID nonExistingPlayer = UUID.randomUUID();

        when(playerProfileRepository.findById(nonExistingPlayer)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> {
            playerProfileService.deleteProfile(nonExistingPlayer);
        });
    }

    @Test
    void testHandleGameCompleted_ShouldUpdateGamesPlayedAndSendStats() {
        GameCompletedEvent event = new GameCompletedEvent(playerId, true);
        PlayerProfile profile = new PlayerProfile();
        profile.setGamesPlayed(5);
        profile.setGamesWon(2);

        when(playerProfileRepository.findById(playerId)).thenReturn(Optional.of(profile));

        playerProfileService.handleGameCompleted(event);

        verify(playerProfileRepository, times(1)).save(any(PlayerProfile.class));
        assertEquals(6, profile.getGamesPlayed());
        assertEquals(3, profile.getGamesWon());
    }

    @Test
    void testHandleLeaderboardUpdated_ShouldUpdateLeaderboardPosition() {
        LeaderboardUpdatedEvent event = new LeaderboardUpdatedEvent(playerId, 10);
        PlayerProfile profile = new PlayerProfile();
        profile.setPlayerId(playerId);

        when(playerProfileRepository.findById(playerId)).thenReturn(Optional.of(profile));

        playerProfileService.handleLeaderboardUpdated(event);

        assertEquals(10, profile.getLeaderboardPosition());
        verify(playerProfileRepository, times(1)).save(profile);
    }

    @Test
    void testUpdateWallet_ShouldUpdateWallet_WithPositiveChange() {
        WalletDTO walletDTO = new WalletDTO(100, 5);
        PlayerProfile profile = new PlayerProfile();
        profile.setWallet(50);

        when(playerProfileRepository.findById(playerId)).thenReturn(Optional.of(profile));

        WalletDTO result = playerProfileService.updateWallet(token, walletDTO);

        assertEquals(105, result.getBalance());
        assertEquals(5, result.getChangeAmount());
        verify(playerProfileRepository, times(1)).save(profile);
    }

    @Test
    void testUpdateWallet_ShouldUpdateWallet_WithNegativeChange() {
        WalletDTO walletDTO = new WalletDTO(100, -10);
        PlayerProfile profile = new PlayerProfile();
        profile.setWallet(100);

        when(playerProfileRepository.findById(playerId)).thenReturn(Optional.of(profile));

        WalletDTO result = playerProfileService.updateWallet(token, walletDTO);

        assertEquals(90, result.getBalance());
        assertEquals(-10, result.getChangeAmount());
        verify(playerProfileRepository, times(1)).save(profile);
    }

    @Test
    void testUpdateWallet_ShouldNotAllowNegativeBalance() {
        WalletDTO walletDTO = new WalletDTO(100, -150);
        PlayerProfile profile = new PlayerProfile();
        profile.setWallet(50);

        when(playerProfileRepository.findById(playerId)).thenReturn(Optional.of(profile));

        assertThrows(NegativeBalanceException.class, () -> {
            playerProfileService.updateWallet(token, walletDTO);
        });
    }


    @Test
    void testGetWallet_ShouldReturnWalletDetails() {
        PlayerProfile profile = new PlayerProfile();
        profile.setWallet(150);

        when(playerProfileRepository.findById(playerId)).thenReturn(Optional.of(profile));

        WalletDTO result = playerProfileService.getWallet(token);

        assertNotNull(result);
        assertEquals(150, result.getBalance());
    }


}