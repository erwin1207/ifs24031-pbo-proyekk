package org.delcom.app.repositories;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

// Import library AssertJ (sudah ada di pom.xml via spring-boot-starter-test)
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Konfigurasi khusus untuk mengetes Layer Repository/Database
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager; // Helper untuk insert data dummy ke DB H2

    @Test
    @DisplayName("Test Find By Email - User Ditemukan")
    void testFindFirstByEmail_Found() {
        // 1. Arrange (Siapkan Data)
        User user = new User("Budi Santoso", "budi@delcom.org", "rahasia123");
        
        // Simpan ke database sementara (H2)
        entityManager.persistAndFlush(user);

        // 2. Act (Jalankan Method Repository)
        Optional<User> foundUser = userRepository.findFirstByEmail("budi@delcom.org");

        // 3. Assert (Cek Hasil)
        assertThat(foundUser).isPresent(); // Data harus ketemu
        assertThat(foundUser.get().getName()).isEqualTo("Budi Santoso");
        assertThat(foundUser.get().getId()).isNotNull(); // ID harus sudah digenerate
    }

    @Test
    @DisplayName("Test Find By Email - User Tidak Ditemukan")
    void testFindFirstByEmail_NotFound() {
        // 1. Arrange
        // Tidak ada user yang disimpan

        // 2. Act
        Optional<User> foundUser = userRepository.findFirstByEmail("hantu@delcom.org");

        // 3. Assert
        assertThat(foundUser).isEmpty(); // Harus kosong (Optional.empty)
    }

    @Test
    @DisplayName("Test Save & Delete User (CRUD Dasar)")
    void testSaveAndDelete() {
        // 1. Save
        User user = new User("Delete Me", "delete@delcom.org", "pass");
        User savedUser = userRepository.save(user);
        
        assertThat(savedUser.getId()).isNotNull();

        // 2. Find
        Optional<User> found = userRepository.findById(savedUser.getId());
        assertThat(found).isPresent();

        // 3. Delete
        userRepository.delete(savedUser);

        // 4. Verify Delete
        Optional<User> deleted = userRepository.findById(savedUser.getId());
        assertThat(deleted).isEmpty();
    }
}