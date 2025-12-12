package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID; // Tambahkan import UUID

import static org.assertj.core.api.Assertions.assertThat;

class AuthContextTest {

    @Test
    @DisplayName("Test Default State (Belum Login)")
    void testInitialState() {
        AuthContext authContext = new AuthContext();
        assertThat(authContext.getAuthUser()).isNull();
        assertThat(authContext.isAuthenticated()).isFalse();
    }

    @Test
    @DisplayName("Test Set User (Sudah Login)")
    void testSetAuthUser() {
        AuthContext authContext = new AuthContext();
        
        User user = new User();
        user.setId(UUID.randomUUID()); // PERBAIKAN: Pakai UUID, bukan 1L
        user.setEmail("test@delcom.org");

        authContext.setAuthUser(user);

        assertThat(authContext.getAuthUser()).isEqualTo(user);
        assertThat(authContext.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("Test Logout (Set User kembali ke Null)")
    void testLogout() {
        AuthContext authContext = new AuthContext();
        authContext.setAuthUser(new User()); 
        authContext.setAuthUser(null); 

        assertThat(authContext.getAuthUser()).isNull();
        assertThat(authContext.isAuthenticated()).isFalse();
    }
}