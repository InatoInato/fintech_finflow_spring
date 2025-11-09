package com.finflow.finflow.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKeyBase64;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ Token generation
    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 1000L * 60 * 60 * 24); // 24 часа

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    // ✅ Injecting email
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ✅ Universal claim method
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    // ✅ Checking jwt expiration
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    // ✅ Checking valid token
    public boolean isTokenValid(String token, String userEmail) {
        try {
            String tokenEmail = extractEmail(token);
            return tokenEmail != null
                    && tokenEmail.equals(userEmail)
                    && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }
}
