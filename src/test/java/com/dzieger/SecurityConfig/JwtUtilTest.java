package com.dzieger.SecurityConfig;

import com.dzieger.config.Parameters;
import com.dzieger.models.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class JwtUtilTest {

    @Mock
    private Parameters parameters;

    @InjectMocks
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private FilterChain filterChain;

    private Key secretKey;
    private final String jwtSecret = "thisisaverysecuresecretkeyforsigningjwt123";
    private final String jwtIssuer = "testIssuer";
    private final long jwtExpiration = 3600000;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setup() throws ServletException, IOException {
        MockitoAnnotations.openMocks(this);

        // Mock Parameters values
        when(parameters.getJwtSecret()).thenReturn(jwtSecret);
        when(parameters.getJwtIssuer()).thenReturn(jwtIssuer);
        when(parameters.getJwtExpiration()).thenReturn(String.valueOf(jwtExpiration));

        // Initialize JwtUtil with mocked parameters
        jwtUtil = new JwtUtil(parameters);
        jwtUtil.init();

        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
        doAnswer(invocation -> null).when(filterChain).doFilter(request, response);
    }

    // Helper method to generate a token
    public String generateToken() {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .claim("username", "testUsername")
                .claim("authorities", List.of(Role.PLAYER.name()))
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
    }

    private String generateExpiredToken() {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .claim("username", "testUsername")
                .claim("authorities", List.of(Role.PLAYER.name()))
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // Token issued 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // Expired 1 hour ago
                .signWith(secretKey)
                .compact();
    }

    @Test
    void testJwtUtilAuthenticate_ValidToken() throws Exception {
        String token = generateToken();

        request.addHeader("Authorization", "Bearer " + token);

        // Simulate filter processing
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNotNull();
        assertTrue(context.getAuthentication().isAuthenticated());
    }

    @Test
    void testJwtUtilAuthenticate_NoToken() throws Exception {
        // Simulate request without Authorization header
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        SecurityContext context = SecurityContextHolder.getContext();
        assertNull(context.getAuthentication());
    }

    @Test
    void testJwtUtilAuthenticate_ExpiredToken() throws Exception {
        String expiredToken = generateExpiredToken();

        request.addHeader("Authorization", "Bearer " + expiredToken);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        SecurityContext context = SecurityContextHolder.getContext();
        assertNull(context.getAuthentication());
    }

    @Test
    void testJwtUtilAuthenticate_InvalidToken() throws Exception {
        String invalidToken = "invalid.token.value";

        request.addHeader("Authorization", "Bearer " + invalidToken);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        SecurityContext context = SecurityContextHolder.getContext();
        assertNull(context.getAuthentication());
    }
}
