package com.example.yourprojectname.service;

import java.util.Map;

public interface TokenService {

    /**
     * Validates a given token for a specific required role.
     * * @param token The user's authentication token.
     * @param requiredRole The role (e.g., "admin", "doctor") required to access the resource.
     * @return An empty Map if the token is valid and the role matches; 
     * a non-empty Map (e.g., {"error": "Invalid Token"}) otherwise.
     */
    Map<String, String> validateToken(String token, String requiredRole);
}
