package org.delcom.app.utils;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    // --- 1. SOLUSI BARIS 10 MERAH (Class Definition) ---
    @Test
    @DisplayName("Test Constructor (Coverage Fix)")
    void testConstructor() {
        // Kita perlu menginstansiasi class ini sekali saja agar
        // Jacoco menandai definisi class sebagai "covered".
        assertNotNull(new JwtUtil());
    }

    // --- 2. SOLUSI BARIS 58 KUNING (Expired Token Logic) ---
    @Test
    @DisplayName("Test Expired Token (Ignore vs Not Ignore)")
    void testExpiredTokenLogic() {
        // Ambil key asli dari utility agar signature valid
        SecretKey key = JwtUtil.getKey();
        UUID userId = UUID.randomUUID();

        // BUAT TOKEN MANUAL YANG SUDAH KEDALUWARSA (Expired 1 detik lalu)
        // Kita tidak bisa pakai JwtUtil.generateToken() karena expirednya lama (2 jam)
        String expiredToken = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 10000)) // Dibuat 10 detik lalu
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 detik lalu
                .signWith(key)
                .compact();

        // CASE A: ignoreExpired = TRUE
        // Harusnya: Masuk catch -> Masuk if(true) -> Return TRUE
        // Ini menutup sisi "True" dari diamond kuning
        boolean resultIgnored = JwtUtil.validateToken(expiredToken, true);
        assertTrue(resultIgnored, "Harusnya TRUE karena expired di-ignore");

        // CASE B: ignoreExpired = FALSE
        // Harusnya: Masuk catch -> Masuk if(false) -> Lanjut ke bawah -> Return FALSE
        // Ini menutup sisi "False" dari diamond kuning
        boolean resultStrict = JwtUtil.validateToken(expiredToken, false);
        assertFalse(resultStrict, "Harusnya FALSE karena expired tidak di-ignore");
    }

    // --- 3. TEST STANDAR LAINNYA (Agar tetap 100%) ---

    @Test
    @DisplayName("Test getKey")
    void testGetKey() {
        assertNotNull(JwtUtil.getKey());
    }

    @Test
    @DisplayName("Test Generate & Extract Valid Token")
    void testGenerateAndExtract() {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);

        assertNotNull(token);
        assertEquals(userId, JwtUtil.extractUserId(token));
        
        // Test validate standard
        assertTrue(JwtUtil.validateToken(token, false));
    }

    @Test
    @DisplayName("Test Extract Invalid Token")
    void testExtractInvalid() {
        // Token rusak/sampah
        String invalidToken = "ini.token.palsu";
        assertNull(JwtUtil.extractUserId(invalidToken));
    }

    @Test
    @DisplayName("Test Validate Invalid Token")
    void testValidateInvalid() {
        // Token rusak/sampah
        assertFalse(JwtUtil.validateToken("token_sampah", false));
    }
}