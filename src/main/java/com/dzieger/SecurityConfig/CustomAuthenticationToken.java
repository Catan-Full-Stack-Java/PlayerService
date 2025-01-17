package com.dzieger.SecurityConfig;

import com.dzieger.models.enums.Role;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final String userId;
    private final String role;

    public CustomAuthenticationToken(String userId, Role role) {
        super(null);
        this.userId = userId;
        this.role = role.toString();
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
