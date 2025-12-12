package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; // PENTING: Import untuk unit waktu

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within; // PENTING: Import untuk toleransi waktu

@DataJpaTest // Menggunakan database H2 in-memory untuk testing JPA
class UserTest {

    @Autowired
    private TestEntityManager entityManager; // Helper sakti untuk manipulasi DB di test

    @Test
    @DisplayName("Test Save User & Auto Generate UUID")
    void testSaveUser() {
        // 1. Arrange
        User user = new User("Budi Santoso", "budi@delcom.org", "rahasia123");

        // 2. Act
        // Simpan ke DB H2 dan paksa commit/flush agar trigger lifecycle jalan
        User savedUser = entityManager.persistAndFlush(user);

        // 3. Assert
        // Pastikan ID sudah digenerate (UUID)
        assertThat(savedUser.getId()).isNotNull();
        
        // Cek kesesuaian data
        assertThat(savedUser.getName()).isEqualTo("Budi Santoso");
        assertThat(savedUser.getEmail()).isEqualTo("budi@delcom.org");
        assertThat(savedUser.getPassword()).isEqualTo("rahasia123");

        // Cek apakah timestamp otomatis terisi
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        
        // PERBAIKAN: Gunakan isCloseTo() alih-alih isEqualTo()
        // Karena LocalDateTime.now() dipanggil terpisah untuk createdAt dan updatedAt,
        // pasti ada selisih waktu sepersekian milidetik/nanodetik.
        // Kita beri toleransi 1 detik (sangat aman).
        assertThat(savedUser.getCreatedAt()).isCloseTo(savedUser.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("Test Update User (@PreUpdate Logic)")
    void testUpdateUser() {
        // 1. Arrange & Save Awal
        User user = new User("Ani", "ani@delcom.org", "pass");
        User savedUser = entityManager.persistAndFlush(user);
        
        // Detach dari session agar saat find, dia benar-benar ambil state baru
        entityManager.flush();
        entityManager.clear();

        // 2. Act
        User foundUser = entityManager.find(User.class, savedUser.getId());
        foundUser.setName("Ani Wijaya"); // Ubah data
        
        // Simpan perubahan
        User updatedUser = entityManager.persistAndFlush(foundUser);

        // 3. Assert
        assertThat(updatedUser.getName()).isEqualTo("Ani Wijaya");
        
        // Pastikan UpdatedAt ada isinya (Logic @PreUpdate jalan)
        assertThat(updatedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Test Constructors")
    void testConstructors() {
        // Test Constructor 1: User(email, password)
        User user1 = new User("email@test.com", "pass");
        assertThat(user1.getEmail()).isEqualTo("email@test.com");
        assertThat(user1.getName()).isEmpty(); // Sesuai logic: this("", email, password)

        // Test Constructor 2: User(name, email, password)
        User user2 = new User("Nama", "email@test.com", "pass");
        assertThat(user2.getName()).isEqualTo("Nama");
    }

    @Test
    @DisplayName("Test ID Immutability (Logic JPA)")
    void testIdGenerationStrategy() {
        User user = new User("Tes", "tes@mail.com", "pass");
        
        // Sebelum save ID null
        assertThat(user.getId()).isNull();
        
        User saved = entityManager.persistAndFlush(user);
        
        // Setelah save ID ada
        assertThat(saved.getId()).isNotNull();
    }
}