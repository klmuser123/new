package com.example.yourprojectname.service;

import com.example.yourprojectname.repository.AdminRepository;
import com.example.yourprojectname.repository.DoctorRepository;
import com.example.yourprojectname.repository.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Service class to handle JWT token generation, extraction, and validation.
 * Implements the TokenService interface contract.
 */
@Component
public class TokenService implements TokenService {

    // Injects the secret key from application properties
    @Value("${jwt.secret}")
    private String secret;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public TokenService(
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // -------------------------------------------------------------------------
    // --- JWT Utility Helpers ---
    // -------------------------------------------------------------------------

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
    
    // -------------------------------------------------------------------------
    // --- Public Utility Methods (Used by other services) ---
    // -------------------------------------------------------------------------

    /**
     * Generates a JWT token for a user.
     * @param userId The unique database ID of the user.
     * @param role The user's role.
     * @return The generated JWT token string.
     */
    public String generateToken(Long userId, String role) {
        long expirationTime = TimeUnit.DAYS.toMillis(7);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the user ID (stored as the token subject).
     */
    public Long extractUserId(String token) {
        try {
            // Note: extractIdentifier returns the subject as a String.
            return Long.parseLong(extractClaim(token, Claims::getSubject));
        } catch (Exception e) {
            return null; 
        }
    }
    
    /**
     * Extracts the identifier (subject) from a JWT token.
     */
    public String extractIdentifier(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Placeholder, as email is not explicitly stored as a claim here
    public String extractUserEmail(String token) {
        return null; 
    }

    // -------------------------------------------------------------------------
    // --- Interface Implementation: TokenService ---
    // -------------------------------------------------------------------------

    /**
     * Validates the JWT token for a given required role.
     * * @param token The user's authentication token.
     * @param requiredRole The role required to access the resource.
     * @return An empty Map if the token is valid and the role matches; a non-empty Map otherwise.
     */
    @Override
    public Map<String, String> validateToken(String token, String requiredRole) {
        try {
            Claims claims = extractAllClaims(token);
            
            // 1. Check expiration
            if (isTokenExpired(token)) {
                return Collections.singletonMap("error", "Token expired.");
            }

            Long userId = claims.get("userId", Long.class);
            String tokenRole = claims.get("role", String.class);

            // 2. Check Role Match and User ID presence
            if (userId == null || !tokenRole.equalsIgnoreCase(requiredRole)) {
                return Collections.singletonMap("error", "Token has incorrect user ID or role.");
            }

            // 3. Database Validation: Check if the user still exists
            boolean userExists = switch (requiredRole.toLowerCase()) {
                case "admin" -> adminRepository.existsById(userId);
                case "doctor" -> doctorRepository.existsById(userId);
                case "patient" -> patientRepository.existsById(userId);
                default -> false;
            };

            if (!userExists) {
                return Collections.singletonMap("error", "User associated with token does not exist.");
            }
            
            // If all checks pass, include user ID and role for downstream services and return an empty map
            Map<String, String> successResult = new HashMap<>();
            successResult.put("userId", String.valueOf(userId));
            successResult.put("role", tokenRole);
            
            // Per the DashboardController logic: If not empty: Redirect to login page.
            // But per the interface definition: Return an EMPTY Map if token is valid.
            // We adjust to meet the interface/controller contract:
            return Collections.emptyMap(); 
            
        } catch (ExpiredJwtException e) {
            return Collections.singletonMap("error", "Token expired.");
        } catch (Exception e) {
            // Catches signature exceptions, malformed tokens, etc.
            return Collections.singletonMap("error", "Invalid or malformed token.");
        }
    }
}
