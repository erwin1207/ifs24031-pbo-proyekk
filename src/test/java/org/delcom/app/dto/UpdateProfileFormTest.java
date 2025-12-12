package org.delcom.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile; // Bawaan spring-boot-starter-test

// Import AssertJ
import static org.assertj.core.api.Assertions.assertThat;

class UpdateProfileFormTest {

    @Test
    @DisplayName("Test Getter & Setter dengan Data Lengkap")
    void testGetterSetter() {
        // 1. Arrange (Siapkan Data)
        UpdateProfileForm form = new UpdateProfileForm();
        String nama = "Budi Santoso";
        
        // Membuat simulasi file gambar
        MockMultipartFile mockPhoto = new MockMultipartFile(
                "fotoProfil",          // nama field form
                "avatar.png",          // nama file asli
                "image/png",           // content type
                "bytes-gambar".getBytes() // isi file dummy
        );

        // 2. Act (Set Data)
        form.setNamaLengkap(nama);
        form.setFotoProfil(mockPhoto);

        // 3. Assert (Cek Data)
        assertThat(form.getNamaLengkap()).isEqualTo("Budi Santoso");
        
        // Cek objek MultipartFile
        assertThat(form.getFotoProfil()).isNotNull();
        assertThat(form.getFotoProfil().getOriginalFilename()).isEqualTo("avatar.png");
        assertThat(form.getFotoProfil().getContentType()).isEqualTo("image/png");
        assertThat(form.getFotoProfil().getSize()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Test Null Handling")
    void testNullValues() {
        // Karena di class aslinya tidak ada anotasi @NotNull,
        // kita pastikan class ini aman jika diisi null.
        
        // 1. Arrange
        UpdateProfileForm form = new UpdateProfileForm();

        // 2. Act
        form.setNamaLengkap(null);
        form.setFotoProfil(null);

        // 3. Assert
        assertThat(form.getNamaLengkap()).isNull();
        assertThat(form.getFotoProfil()).isNull();
    }
}