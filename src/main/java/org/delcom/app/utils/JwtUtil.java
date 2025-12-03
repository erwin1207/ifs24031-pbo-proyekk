package org.delcom.app.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

public class JwtUtil {

    // Ganti dengan secret key yang lebih aman dan simpan di tempat yang aman
    private static final String SECRET_KEY = "NghR8fQn5O6V2z7VwpvQkDELCOMXoCYQbQZjx3xWUpPfw5i9L8RrGg==";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2 jam
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public static SecretKey getKey() {
        return key;
    }

    public static String generateToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public static UUID extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validasi token
     * 
     * @param token         JWT token
     * @param ignoreExpired jika true maka token expired tetap dianggap valid
     */
    public static boolean validateToken(String token, boolean ignoreExpired) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true; // valid
        } catch (ExpiredJwtException e) {
            if (ignoreExpired) {
                return true; // abaikan expired
            }
            return false;
        } catch (Exception e) {
            return false; // token invalid
        }
    }
}