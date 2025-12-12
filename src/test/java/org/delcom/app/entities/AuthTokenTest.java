package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Load konfigurasi JPA dan H2 Database
class AuthTokenTest {

    @Autowired
    private TestEntityManager entityManager; // Helper untuk manipulasi DB di test

    @Test
    @DisplayName("Test Save Entity & Auto Generated UUID")
    void testSaveAuthToken() {
        // 1. Arrange
        UUID userId = UUID.randomUUID();
        String tokenString = "jwt.token.dummy.12345";
        
        // Buat object baru (ID masih null di sini)
        AuthToken authToken = new AuthToken(userId, tokenString);

        // 2. Act
        // Simpan ke DB H2 & Flush (paksa tulis sekarang)
        AuthToken savedToken = entityManager.persistAndFlush(authToken);

        // 3. Assert
        // Cek ID (Harus sudah digenerate oleh Hibernate)
        assertThat(savedToken.getId()).isNotNull();
        
        // Cek Data
        assertThat(savedToken.getUserId()).isEqualTo(userId);
        assertThat(savedToken.getToken()).isEqualTo(tokenString);
        
        // Cek CreatedAt (Harus ada isinya)
        assertThat(savedToken.getCreatedAt()).isNotNull();
        assertThat(savedToken.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test Constructor Logic (POJO)")
    void testConstructorLogic() {
        // Test logic Java biasa tanpa DB
        // Constructor kamu: this.createdAt = LocalDateTime.now();
        
        UUID uid = UUID.randomUUID();
        AuthToken token = new AuthToken(uid, "abc");

        assertThat(token.getUserId()).isEqualTo(uid);
        assertThat(token.getToken()).isEqualTo("abc");
        
        // Pastikan constructor sudah mengisi createdAt
        assertThat(token.getCreatedAt()).isNotNull(); 
    }

    @Test
    @DisplayName("Test @PrePersist Logic")
    void testPrePersist() {
        // 1. Arrange
        // Kita pakai default constructor (createdAt null)
        AuthToken token = new AuthToken(); 
        token.setUserId(UUID.randomUUID());
        token.setToken("token_manual");

        // 2. Act
        AuthToken savedToken = entityManager.persistAndFlush(token);
        
        // 3. Assert
        // @PrePersist protected void onCreate() harusnya jalan otomatis
        assertThat(savedToken.getCreatedAt()).isNotNull();
    }

    // --- TAMBAHAN PENTING DI SINI ---
    @Test
    @DisplayName("Test Setters Getters Manual (Fix Coverage setId)")
    void testManualSettersAndGetters() {
        // Arrange
        AuthToken token = new AuthToken();
        UUID mockId = UUID.randomUUID();
        UUID mockUserId = UUID.randomUUID();
        
        // Act: Panggil setId secara EKSPLISIT agar baris merah jadi hijau
        token.setId(mockId);
        token.setUserId(mockUserId);
        token.setToken("manual-token");

        // Assert
        assertThat(token.getId()).isEqualTo(mockId);
        assertThat(token.getUserId()).isEqualTo(mockUserId);
        assertThat(token.getToken()).isEqualTo("manual-token");
    }
}