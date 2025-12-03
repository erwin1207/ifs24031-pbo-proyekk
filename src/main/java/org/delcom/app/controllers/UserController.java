package org.delcom.app.controllers;

import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final AuthTokenService authTokenService;

    public UserController(UserService userService, AuthTokenService authTokenService) {
        this.userService = userService;
        this.authTokenService = authTokenService;
    }

    @Autowired
    protected AuthContext authContext;

    // Melakukan registrasi pengguna
    // -------------------------------
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<Map<String, UUID>>> registerUser(@RequestBody User reqUser) {
        if (reqUser.getName() == null || reqUser.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data nama tidak valid", null));
        } else if (reqUser.getEmail() == null || reqUser.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data email tidak valid", null));
        } else if (reqUser.getPassword() == null || reqUser.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data password tidak valid", null));
        }

        // Cek apakah email sudah terdaftar
        User existingUser = userService.getUserByEmail(reqUser.getEmail());
        if (existingUser != null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Pengguna sudah terdaftar dengan email ini",
                            null));
        }

        String hashPassword = new BCryptPasswordEncoder().encode(reqUser.getPassword());

        User createdUser = userService.createUser(
                reqUser.getName(),
                reqUser.getEmail(),
                hashPassword);

        return ResponseEntity.ok().body(new ApiResponse<>(
                "success",
                "Berhasil melakukan pendaftaran",
                Map.of("id", createdUser.getId())));
    }

    // Melakukan login pengguna
    // -------------------------------
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> loginUser(@RequestBody User reqUser) {
        if (reqUser.getEmail() == null || reqUser.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data tidak valid", null));
        } else if (reqUser.getPassword() == null || reqUser.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data tidak valid", null));
        }

        User existingUser = userService.getUserByEmail(reqUser.getEmail());
        if (existingUser == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Email atau password salah", null));
        }

        boolean isPasswordMatch = new BCryptPasswordEncoder()
                .matches(reqUser.getPassword(), existingUser.getPassword());
        if (!isPasswordMatch) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Email atau password salah", null));
        }

        String jwtToken = JwtUtil.generateToken(existingUser.getId());

        // Hapus token lama jika ada
        AuthToken existingAuthToken = authTokenService.findUserToken(existingUser.getId(), jwtToken);
        if (existingAuthToken != null) {
            authTokenService.deleteAuthToken(existingUser.getId());
        }

        AuthToken authToken = new AuthToken(existingUser.getId(), jwtToken);
        var createdAuthToken = authTokenService.createAuthToken(authToken);
        if (createdAuthToken == null) {
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Gagal membuat token autentikasi", null));
        }

        return ResponseEntity.ok().body(new ApiResponse<>(
                "success",
                "Login berhasil",
                Map.of("authToken", jwtToken)));
    }

    // Get informasi pengguna
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<Map<String, User>>> getUserInfo() {

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Data tidak valid", null));
        }
        User authUser = authContext.getAuthUser();
        authUser.setPassword(null); // Sembunyikan password dalam response

        ApiResponse<Map<String, User>> response = new ApiResponse<>("success", "Berhasil mendapatkan info user",
                Map.of("user", authUser));
        return ResponseEntity.ok(response);
    }

    // Mengubah informasi pengguna
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<User>> updateUser(@RequestBody User reqUser) {

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Data tidak valid", null));
        }
        User authUser = authContext.getAuthUser();

        if (reqUser.getName() == null || reqUser.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data nama tidak valid", null));
        } else if (reqUser.getEmail() == null || reqUser.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Data email tidak valid", null));
        }

        User updatedUser = userService.updateUser(
                authUser.getId(),
                reqUser.getName(),
                reqUser.getEmail());
        if (updatedUser == null) {
            ApiResponse<User> response = new ApiResponse<>("fail", "User tidak ditemukan", null);
            return ResponseEntity.status(404).body(response);
        }

        ApiResponse<User> response = new ApiResponse<>("success", "User berhasil diupdate", null);
        return ResponseEntity.ok(response);
    }

    // Mengubah password pengguna
    @PutMapping("/users/me/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(@RequestBody Map<String, String> passwordPayload) {

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>("fail", "Autentikasi tidak valid", null));
        }

        User authUser = authContext.getAuthUser();

        // Ambil old & new password
        String oldPassword = passwordPayload.get("password");
        String newPassword = passwordPayload.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty() ||
                newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Password lama dan baru wajib diisi", null));
        }

        // Validasi password lama
        boolean isPasswordMatch = new BCryptPasswordEncoder()
                .matches(oldPassword, authUser.getPassword());
        if (!isPasswordMatch) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Konfirmasi password tidak cocok", null));
        }

        // Update password baru
        String hashPassword = new BCryptPasswordEncoder().encode(newPassword);
        User updatedUser = userService.updatePassword(authUser.getId(), hashPassword);
        if (updatedUser == null) {
            ApiResponse<Void> response = new ApiResponse<>("fail", "User tidak ditemukan", null);
            return ResponseEntity.status(404).body(response);
        }

        // Hapus token lama setelah password diubah
        authTokenService.deleteAuthToken(authUser.getId());

        return ResponseEntity.ok(new ApiResponse<>("success", "Password berhasil diupdate", null));
    }

}
