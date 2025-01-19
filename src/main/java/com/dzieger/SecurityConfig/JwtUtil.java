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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

    @Autowired
    private Parameters params;

    private final Environment env;

    public JwtUtil(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing JwtUtil");

        String[] activeProfiles = env.getActiveProfiles();
        boolean isTestProfile = List.of(activeProfiles).contains("test");

        if (!isTestProfile) {
            if (params != null) {
                this.jwtExpiration = Long.parseLong(params.getJwtExpiration());
                this.jwtSecret = params.getJwtSecret();
                this.jwtIssuer = params.getJwtIssuer();
            } else {
                log.warn("Parameters bean is not available, using default values");
                this.jwtExpiration = 360000;
                this.jwtSecret = "thisisaverysecretcodethatshouldnotbeshared";
                this.jwtIssuer = "testIssuer";
            }
        } else {
            this.jwtExpiration = 360000;
            this.jwtSecret = "thisisaverysecretcodethatshouldnotbeshared";
            this.jwtIssuer = "testIssuer";
        }

        this.KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Claims extractAllClaims(String token) {
        log.info("Extracting all claims from token");
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("Error while extracting claims from token", e);
            throw new JwtException("Error while extracting claims from token");
        }
    }

    public String extractUserId(String token) {
        log.info("Extracting user ID from token");
        return extractAllClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        log.info("Extracting username from token");
        return extractAllClaims(token).get("username", String.class);
    }

    public List<Role> extractRole(String token) {
        log.info("Extracting role from token");
        List<String> roles = extractAllClaims(token).get("authorities", List.class);
        return roles.stream().map(Role::valueOf).toList(); // Convert strings to Role enums
    }

    public boolean validate(String token) {
        log.info("Validating token");
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
