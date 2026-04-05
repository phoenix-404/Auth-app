package com.example.auth_app_backend.security;

import com.example.auth_app_backend.entities.Role;
import com.example.auth_app_backend.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JWTService {
    // Configuration values usually stored in application.properties/yml
    private final long accessTtlSeconds;  // Time To Live for access tokens
    private final long refreshTtlSeconds; // Time To Live for refresh tokens
    private final String issuer;          // Who created this token (e.g., "my-auth-server")
    private final SecretKey key;          // The cryptographic key used to sign the tokens

    public JWTService(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.access-ttl-seconds}")long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer) {

        // SECURITY CHECK: JWT HS512 algorithm requires at least a 512-bit (64-byte) key
        // to prevent brute-force attacks.
        if(secretKey == null || secretKey.length()<64){
            throw new IllegalArgumentException("Invalid Secret: Key must be at least 64 bytes (512 bits)");
        }

        // Convert the raw string secret into a secure HMAC-SHA key
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    /**
     * Creates a token containing the user's identity and roles.
     * This is what the frontend sends in the 'Authorization: Bearer <token>' header.
     */
    public String generateAccessToken(User user){
        Instant now = Instant.now();
        // Extract role names (e.g., "ROLE_USER", "ROLE_ADMIN") from the user object
        List<String> roles = user.getRoles() == null ? List.of():
                user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // Unique ID for this specific token
                .subject(user.getId().toString())  // Who this token belongs to (User ID)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds))) // Sets the expiry
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "typ", "access" // Custom claim to distinguish token type
                ))
                .signWith(key) // Digitally sign the token so it cannot be tampered with
                .compact();    // Serialize into a URL-safe string
    }

    /**
     * Creates a simpler token used only to request new Access Tokens.
     * Usually does not contain roles/email to keep it lightweight.
     */
    public String generateRefreshToken(User user, String jtid) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jtid) // Often linked to a database record for "Refresh Token Rotation"
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ","refresh")
                .signWith(key)
                .compact();
    }

    /**
     * The "Validator": Checks if the signature is valid and the token isn't expired.
     * If the token was tampered with, this throws a JwtException.
     */
    public Jws<Claims> parse(String token){
        try{
            return Jwts.parser()
                    .verifyWith(key) // Use the same key used for signing
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e){
            throw e; // Pass the error (Expired, Invalid Signature, etc.) up the chain
        }
    }

    // --- Helper Methods to extract specific data (Claims) from the token ---

    public boolean isAccessToken(String token){
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token){
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserID(String token){
        Claims c = parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    public String getJti(String token){
        return parse(token).getPayload().getId();
    }

    public List<String> getRoles(String token){
        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }

    public String getEmail(String token){
        Claims c = parse(token).getPayload();
        return (String) c.get("email");
    }
}
