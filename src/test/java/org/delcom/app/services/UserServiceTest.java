package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Aktifkan Mockito (Cepat & Tanpa DB asli)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // --- TEST CREATE ---

    @Test
    @DisplayName("Create User - Panggil Repository Save")
    void testCreateUser() {
        // 1. Arrange
        String name = "Budi";
        String email = "budi@delcom.org";
        String password = "hash_password";

        User savedUser = new User(name, email, password);
        
        // Simulasi repository save berhasil
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // 2. Act
        User result = userService.createUser(name, email, password);

        // 3. Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
        
        // Pastikan fungsi save dipanggil 1 kali
        verify(userRepository, times(1)).save(any(User.class));
    }

    // --- TEST GET BY EMAIL ---

    @Test
    @DisplayName("Get User By Email - Ditemukan")
    void testGetUserByEmail_Found() {
        // 1. Arrange
        String email = "test@delcom.org";
        User user = new User();
        user.setEmail(email);

        // Mock return Optional berisi user
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.of(user));

        // 2. Act
        User result = userService.getUserByEmail(email);

        // 3. Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Get User By Email - Tidak Ditemukan")
    void testGetUserByEmail_NotFound() {
        // Mock return Optional kosong
        when(userRepository.findFirstByEmail(anyString())).thenReturn(Optional.empty());

        User result = userService.getUserByEmail("hantu@delcom.org");

        assertThat(result).isNull(); // Service harus return null (sesuai .orElse(null))
    }

    // --- TEST GET BY ID ---

    @Test
    @DisplayName("Get User By ID - Found vs Not Found")
    void testGetUserById() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        // Case Found
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertThat(userService.getUserById(id)).isEqualTo(user);

        // Case Not Found
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThat(userService.getUserById(id)).isNull();
    }

    // --- TEST UPDATE USER ---

    @Test
    @DisplayName("Update User - Sukses")
    void testUpdateUser_Success() {
        // 1. Arrange
        UUID id = UUID.randomUUID();
        User existingUser = new User("Nama Lama", "lama@mail.com", "pass");
        existingUser.setId(id);

        // Mock findById ketemu
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        // Mock save return user yang sudah diubah
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Act
        User updated = userService.updateUser(id, "Nama Baru", "baru@mail.com");

        // 3. Assert
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Nama Baru");   // Cek data berubah
        assertThat(updated.getEmail()).isEqualTo("baru@mail.com");
        
        verify(userRepository).save(existingUser); // Pastikan disave
    }

    @Test
    @DisplayName("Update User - Gagal (User Tidak Ditemukan)")
    void testUpdateUser_NotFound() {
        UUID id = UUID.randomUUID();
        // Mock findById kosong
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updateUser(id, "Baru", "baru@mail.com");

        assertThat(result).isNull();
        // Pastikan save TIDAK pernah dipanggil
        verify(userRepository, never()).save(any());
    }

    // --- TEST UPDATE PASSWORD ---

    @Test
    @DisplayName("Update Password - Sukses")
    void testUpdatePassword_Success() {
        // 1. Arrange
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setPassword("old_pass");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // 2. Act
        User result = userService.updatePassword(id, "new_hashed_pass");

        // 3. Assert
        assertThat(result).isNotNull();
        assertThat(result.getPassword()).isEqualTo("new_hashed_pass");
        
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Update Password - Gagal (User Tidak Ditemukan)")
    void testUpdatePassword_NotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        User result = userService.updatePassword(UUID.randomUUID(), "new_pass");

        assertThat(result).isNull();
        verify(userRepository, never()).save(any());
    }
}