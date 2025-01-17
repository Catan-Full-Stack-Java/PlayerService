package com.dzieger.SecurityConfig;

import com.dzieger.config.Parameters;
import com.dzieger.models.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private long jwtExpiration;
    private String jwtSecret;
    private String jwtIssuer;
    private Key KEY;

    private final Parameters params;

    public JwtUtil(Parameters params) {
        this.params = params;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing JwtUtil");
        jwtExpiration = Long.parseLong(params.getJwtExpiration());
        jwtSecret = params.getJwtSecret();
        jwtIssuer = params.getJwtIssuer();
        KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public List<Role> extractRole(String token) {
        List<String> roles = extractAllClaims(token).get("authorities", List.class);
        return roles.stream().map(Role::valueOf).toList(); // Convert strings to Role enums
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .setAllowedClockSkewSeconds(1) // Add 1 second of clock skew
                    .build()
                    .parseClaimsJws(token);
            if (!jwtIssuer.equals(extractAllClaims(token).getIssuer())) {
                throw new JwtException("Invalid JWT issuer");
            }
            return jwtIssuer.equals(extractAllClaims(token).getIssuer());
        } catch (JwtException e) {
            log.error("Token not validated");
        }
        return false;
    }

    // Helper method to generate a token
    public String generateToken(String userId, String username, List<Role> roles) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("authorities", roles.stream().map(Role::name).toList())
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(KEY)
                .compact();
    }

}
