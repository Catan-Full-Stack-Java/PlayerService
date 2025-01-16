package com.dzieger.securityConfig;

import com.dzieger.config.Parameters;
import com.dzieger.models.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.List;

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
        return extractAllClaims(token).get("authorities", List.class);
    }

}
