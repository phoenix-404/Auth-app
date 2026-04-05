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
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;
    private final SecretKey key;

    public JWTService(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.access-ttl-seconds}")long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer) {

        // For ensuring secret is long enough for HS512
        if(secretKey == null || secretKey.length()<64){
            throw new IllegalArgumentException("Invalid Secret: Key must be at least 64 bytes (512 bits)");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    //Generate token:
    public String generateAccessToken(User user){
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of():
                user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles",roles,
                        "typ", "access"
                ))
                .signWith(key)
                .compact();
    }

    //Generate RefreshToken:
    public String generateRefreshToken(User user, String jtid) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jtid)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ","refresh")
                .signWith(key)
                .compact();
    }

    //parse the token
    public Jws<Claims> parse(String token){
        try{
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (JwtException e){
            throw e;
        }
    }

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
