package com.geoedu.config;

import com.geoedu.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    
    private static final List<String> VALID_ROLES = Arrays.asList("student", "teacher", "admin");

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId, String username, String role) {
        if (!VALID_ROLES.contains(role.toLowerCase())) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Valid roles are: " + VALID_ROLES);
        }
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    public String getUserIdFrom(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("username", String.class);
    }

    public String getRoleFrom(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    public long getExpiration() {
        return jwtProperties.getExpiration();
    }

    public boolean validate(String token) {
        try {
            getClaims(token);
            return true;
        } catch (SignatureException e) {
            throw new AuthenticationException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            throw new AuthenticationException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            throw new AuthenticationException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("JWT claims string is empty");
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
