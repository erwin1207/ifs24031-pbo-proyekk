package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Aktifkan Mockito tanpa Spring Boot context yang berat
class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository; // Dependency yang dipalsukan

    @InjectMocks
    private AuthTokenService authTokenService; // Service yang sedang dites (repository akan disuntikkan ke sini)

    @Test
    @DisplayName("Test Find User Token - Panggil Repository findUserToken")
    void testFindUserToken() {
        // 1. Arrange (Siapkan Data)
        UUID userId = UUID.randomUUID();
        String tokenStr = "jwt_token_123";
        AuthToken mockToken = new AuthToken(userId, tokenStr);

        // Simulasi: Jika repository dipanggil, kembalikan mockToken
        when(authTokenRepository.findUserToken(userId, tokenStr)).thenReturn(mockToken);

        // 2. Act (Jalankan Service)
        AuthToken result = authTokenService.findUserToken(userId, tokenStr);

        // 3. Assert (Verifikasi)
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(tokenStr);

        // Pastikan service benar-benar memanggil repository 1 kali
        verify(authTokenRepository, times(1)).findUserToken(userId, tokenStr);
    }

    @Test
    @DisplayName("Test Create Auth Token - Panggil Repository save")
    void testCreateAuthToken() {
        // 1. Arrange
        AuthToken newToken = new AuthToken(UUID.randomUUID(), "new_token");
        
        // Simulasi: Repository save mengembalikan object yang sama
        when(authTokenRepository.save(newToken)).thenReturn(newToken);

        // 2. Act
        AuthToken savedToken = authTokenService.createAuthToken(newToken);

        // 3. Assert
        assertThat(savedToken).isEqualTo(newToken);
        
        // Verifikasi pemanggilan method save
        verify(authTokenRepository).save(newToken);
    }

    @Test
    @DisplayName("Test Delete Auth Token - Panggil Repository deleteByUserId")
    void testDeleteAuthToken() {
        // 1. Arrange
        UUID userId = UUID.randomUUID();

        // Note: Method void tidak perlu "when(...).thenReturn(...)"
        // Kita cukup verifikasi apakah dipanggil atau tidak.

        // 2. Act
        authTokenService.deleteAuthToken(userId);

        // 3. Assert
        verify(authTokenRepository, times(1)).deleteByUserId(userId);
    }
}