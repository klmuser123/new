package com.example.yourprojectname.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    // Simple dummy tokens for demonstration
    private static final String VALID_ADMIN_TOKEN = "ADMIN_SECURE_TOKEN_123";
    private static final String VALID_DOCTOR_TOKEN = "DOCTOR_AUTH_TOKEN_456";

    @Override
    public Map<String, String> validateToken(String token, String requiredRole) {
        
        // In a real application, this would involve JWT validation, database lookup, etc.
        if (token == null || token.trim().isEmpty()) {
            return Collections.singletonMap("error", "Token is missing.");
        }

        switch (requiredRole.toLowerCase()) {
            case "admin":
                if (VALID_ADMIN_TOKEN.equals(token)) {
                    return Collections.emptyMap(); // Valid token for admin
                }
                break;
            case "doctor":
                if (VALID_DOCTOR_TOKEN.equals(token)) {
                    return Collections.emptyMap(); // Valid token for doctor
                }
                break;
            default:
                // Role not recognized or handled
                return Collections.singletonMap("error", "Role not supported.");
        }

        // If validation failed
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Invalid token or incorrect role.");
        return errors;
    }
}
