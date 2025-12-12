package org.delcom.app.repositories;

import org.delcom.app.entities.AuthToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Hanya load layer Data/DB
class AuthTokenRepositoryTest {

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private TestEntityManager entityManager; // Helper untuk insert data dummy

    @Test
    @DisplayName("Test Find Token (Custom Query) - Sukses & Gagal")
    void testFindUserToken() {
        // 1. Arrange (Siapkan Data di DB H2)
        UUID userId = UUID.randomUUID();
        String validTokenStr = "token_rahasia_123";
        
        AuthToken token = new AuthToken(userId, validTokenStr);
        entityManager.persistAndFlush(token); // Simpan

        // 2. Act & Assert (Cek Query Custom)
        
        // Kasus A: Data Benar (User ID cocok, Token cocok)
        AuthToken resultFound = authTokenRepository.findUserToken(userId, validTokenStr);
        assertThat(resultFound).isNotNull();
        assertThat(resultFound.getUserId()).isEqualTo(userId);

        // Kasus B: User ID Salah
        AuthToken resultWrongUser = authTokenRepository.findUserToken(UUID.randomUUID(), validTokenStr);
        assertThat(resultWrongUser).isNull();

        // Kasus C: Token Salah
        AuthToken resultWrongToken = authTokenRepository.findUserToken(userId, "token_palsu");
        assertThat(resultWrongToken).isNull();
    }

    @Test
    @DisplayName("Test Delete Token By UserId (Modifying Query)")
    void testDeleteByUserId() {
        // 1. Arrange
        UUID userTarget = UUID.randomUUID();
        UUID userLain = UUID.randomUUID();

        // Simpan 2 token milik user target
        AuthToken t1 = new AuthToken(userTarget, "token_1");
        AuthToken t2 = new AuthToken(userTarget, "token_2");
        entityManager.persist(t1);
        entityManager.persist(t2);

        // Simpan 1 token milik user lain (tidak boleh terhapus)
        AuthToken t3 = new AuthToken(userLain, "token_3");
        entityManager.persist(t3);
        
        entityManager.flush(); // Pastikan masuk DB

        // 2. Act
        authTokenRepository.deleteByUserId(userTarget);
        
        // Flush & Clear cache agar entityManager mengambil data segar dari DB
        entityManager.flush();
        entityManager.clear();

        // 3. Assert
        
        // Token user target harus hilang
        assertThat(authTokenRepository.findById(t1.getId())).isEmpty();
        assertThat(authTokenRepository.findById(t2.getId())).isEmpty();

        // Token user lain harus tetap ada
        Optional<AuthToken> sisaToken = authTokenRepository.findById(t3.getId());
        assertThat(sisaToken).isPresent();
        assertThat(sisaToken.get().getToken()).isEqualTo("token_3");
    }
}