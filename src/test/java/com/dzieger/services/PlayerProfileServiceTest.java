package com.dzieger.services;

import com.dzieger.SecurityConfig.JwtUtil;
import com.dzieger.config.Parameters;
import com.dzieger.dtos.PreferencesDTO;
import com.dzieger.dtos.ProfileDTO;
import com.dzieger.exceptions.ProfileAlreadyExistsException;
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
import org.springframework.test.context.ActiveProfiles;

import java.security.Key;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PlayerProfileServiceTest {

    @Mock
    private PlayerProfileRepository playerProfileRepository;

    @Mock
    private Parameters params;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private PlayerProfileService playerProfileService;

    private static final UUID playerId = UUID.randomUUID();
    private static final String username = "testUser";
    private static final Role role = Role.PLAYER;
    private static final List<String> roles = List.of(role.toString());

    private Key secretKey;
    private final String jwtSecret = "thisisaverysecuresecretkeyforsigningjwt123";
    private final String jwtIssuer = "testIssuer";
    private final long jwtExpiration = 3600000;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        when(params.getJwtSecret()).thenReturn(jwtSecret);
        when(params.getJwtIssuer()).thenReturn(jwtIssuer);
        when(params.getJwtExpiration()).thenReturn(String.valueOf(jwtExpiration));
    }

    // Helper method to generate a token
    private String generateToken() {
        return Jwts.builder()
                .setSubject(playerId.toString())
                .claim("username", username)
                .claim("roles", roles)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
    }

    @Test
    void testCreateProfile_ShouldCreateProfileWithDefaultPreferences() {
        String token = generateToken();
        UUID newPlayerId = UUID.fromString(jwtUtil.extractUserId(token));

        when(playerProfileRepository.existsById(newPlayerId)).thenReturn(false);

        String result = playerProfileService.createProfile(token);

        assertNotNull(result);

        // Verify that the player profile contains the default preferences

        verify(playerProfileRepository, times(1)).save(any(PlayerProfile.class));
    }

    @Test
    void testCreateProfile_ProfileExists_ShouldReturnConflict() {
        String token = generateToken();
        UUID newPlayerId = UUID.fromString(jwtUtil.extractUserId(token));

        when(playerProfileRepository.existsById(newPlayerId)).thenReturn(true);

        assertThrows(ProfileAlreadyExistsException.class, () -> {
            playerProfileService.createProfile(token);
        });
        verify(playerProfileRepository, times(0)).save(any(PlayerProfile.class));
    }

    @Test
    void testGetProfile_ShouldReturnProfile() {
        String token = generateToken();
        UUID playerId = UUID.fromString(jwtUtil.extractUserId(token));
        PlayerProfile playerProfile = new PlayerProfile();
        playerProfile.setPlayerId(playerId);

        when(playerProfileRepository.findById(playerId)).thenReturn(Optional.of(playerProfile));

        ProfileDTO result = playerProfileService.getProfile(token);

        assertNotNull(result);
    }

    @Test
    void updateProfilePreferences_shouldUpdatePreferences() {
        String token = generateToken();
        UUID playerId = UUID.fromString(jwtUtil.extractUserId(token));
        Map<String, Object> newPreferences = Map.of("theme", "dark");

        PreferencesDTO updatedPreferences = new PreferencesDTO();
        updatedPreferences.setPreferences(newPreferences);

        when(playerProfileRepository.findById(playerId)).thenReturn(Optional.of(new PlayerProfile()));
        when(playerProfileRepository.save(any(PlayerProfile.class))).thenReturn(new PlayerProfile());

        PreferencesDTO result = playerProfileService.updateProfilePreferences(token, newPreferences);

        assertNotNull(result);
        assertThat(result.getPreferences()).containsEntry("theme", "dark");
    }

}