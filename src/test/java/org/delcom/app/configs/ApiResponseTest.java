package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Import AssertJ (sudah ada di pom.xml via spring-boot-starter-test)
// Ini membuat kodingan test lebih mudah dibaca seperti bahasa manusia
import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("Test ApiResponse dengan data String")
    void testApiResponseString() {
        // 1. Arrange (Siapkan data)
        String status = "success";
        String message = "Login berhasil";
        String data = "Token12345ABC";

        // 2. Act (Eksekusi)
        ApiResponse<String> response = new ApiResponse<>(status, message, data);

        // 3. Assert (Verifikasi hasil dengan AssertJ)
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getMessage()).isEqualTo("Login berhasil");
        assertThat(response.getData())
                .isNotNull()
                .isEqualTo("Token12345ABC");
    }

    @Test
    @DisplayName("Test ApiResponse dengan data Integer (untuk ID)")
    void testApiResponseInteger() {
        // Generic Type <Integer>
        ApiResponse<Integer> response = new ApiResponse<>("created", "User dibuat", 101);

        assertThat(response.getStatus()).isEqualTo("created");
        assertThat(response.getData())
                .isInstanceOf(Integer.class) // Pastikan tipenya Integer
                .isEqualTo(101);
    }

    @Test
    @DisplayName("Test ApiResponse saat Data Null (Cek handling Generic)")
    void testApiResponseNull() {
        // Misal kasus error, datanya null
        ApiResponse<Object> response = new ApiResponse<>("error", "Data tidak ditemukan", null);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).contains("tidak ditemukan"); // Cek sebagian teks
        assertThat(response.getData()).isNull();
    }
}