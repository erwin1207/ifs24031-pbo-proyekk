package org.delcom.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HealthRecordPhotoFormTest {

    @Test
    @DisplayName("Test Getter & Setter Standard")
    void testGetterSetter() {
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("photo", "test.jpg", "image/jpeg", new byte[10]);

        form.setId(id);
        form.setPhotoFile(file);

        assertThat(form.getId()).isEqualTo(id);
        assertThat(form.getPhotoFile()).isEqualTo(file);
    }

    // --- TAMBAHAN 1: Menghijaukan getOriginalFilename() ---
    @Test
    @DisplayName("Test getOriginalFilename() - File Ada & Null")
    void testGetOriginalFilename() {
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();

        // 1. Case File Ada
        MockMultipartFile file = new MockMultipartFile("f", "hello.png", "image/png", "a".getBytes());
        form.setPhotoFile(file);
        assertThat(form.getOriginalFilename()).isEqualTo("hello.png");

        // 2. Case File Null (Ini yang bikin kuning sebelumnya)
        form.setPhotoFile(null);
        assertThat(form.getOriginalFilename()).isNull();
    }

    @Test
    @DisplayName("Test isEmpty()")
    void testIsEmpty() {
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();

        // Case 1: Null file
        form.setPhotoFile(null);
        assertThat(form.isEmpty()).isTrue();

        // Case 2: File ada tapi kosong (0 bytes)
        MockMultipartFile emptyFile = new MockMultipartFile("photo", new byte[0]);
        form.setPhotoFile(emptyFile);
        assertThat(form.isEmpty()).isTrue();

        // Case 3: File ada isinya
        MockMultipartFile validFile = new MockMultipartFile("photo", "a".getBytes());
        form.setPhotoFile(validFile);
        assertThat(form.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Test isValidImage() - Tipe File Benar (Termasuk GIF)")
    void testIsValidImage_Success() {
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();

        // Test JPEG
        form.setPhotoFile(new MockMultipartFile("f", "f.jpg", "image/jpeg", "content".getBytes()));
        assertThat(form.isValidImage()).isTrue();

        // Test PNG
        form.setPhotoFile(new MockMultipartFile("f", "f.png", "image/png", "content".getBytes()));
        assertThat(form.isValidImage()).isTrue();
        
        // Test GIF (Tambahan agar branch GIF tercover)
        form.setPhotoFile(new MockMultipartFile("f", "f.gif", "image/gif", "content".getBytes()));
        assertThat(form.isValidImage()).isTrue();

        // Test WEBP
        form.setPhotoFile(new MockMultipartFile("f", "f.webp", "image/webp", "content".getBytes()));
        assertThat(form.isValidImage()).isTrue();
    }

    // --- TAMBAHAN 2: Menghijaukan ContentType NULL ---
    @Test
    @DisplayName("Test isValidImage() - Content Type Null")
    void testIsValidImage_NullContentType() {
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();

        // Buat file dengan ContentType NULL
        MockMultipartFile file = new MockMultipartFile("f", "unknown.file", null, "content".getBytes());
        form.setPhotoFile(file);

        // Harusnya false karena contentType != null gagal
        assertThat(form.isValidImage()).isFalse();
    }

    @Test
    @DisplayName("Test isValidImage() - Tipe File Salah")
    void testIsValidImage_Fail() {
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();

        // Case 1: PDF
        form.setPhotoFile(new MockMultipartFile("f", "doc.pdf", "application/pdf", "content".getBytes()));
        assertThat(form.isValidImage()).isFalse();

        // Case 2: Null File
        form.setPhotoFile(null);
        assertThat(form.isValidImage()).isFalse();
    }

    @Test
    @DisplayName("Test isSizeValid() - Validasi Ukuran")
    void testIsSizeValid() {
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();
        
        byte[] content = new byte[100]; 
        MockMultipartFile file = new MockMultipartFile("photo", "pic.jpg", "image/jpeg", content);
        form.setPhotoFile(file);

        // Valid
        assertThat(form.isSizeValid(200)).isTrue();
        // Invalid
        assertThat(form.isSizeValid(50)).isFalse();
        // File Null
        form.setPhotoFile(null);
        assertThat(form.isSizeValid(100)).isFalse();
    }
}