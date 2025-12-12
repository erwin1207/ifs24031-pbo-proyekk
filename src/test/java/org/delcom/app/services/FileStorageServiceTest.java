package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile; // Import tambahan

import java.io.ByteArrayInputStream; // Import tambahan
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;       // Import tambahan
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;       // Import tambahan

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        // Arahkan uploadDir ke sub-folder "uploads" di dalam tempDir
        Path subPath = tempDir.resolve("uploads");
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", subPath.toString());
    }

    // --- 1. Happy Path & Create Directory (Filename normal) ---
    // Mengcover: exists()=false, filename!=null, contains(".")=true
    @Test
    @DisplayName("Store File - Folder Belum Ada (Auto Create)")
    void testStoreFile_CreateDirectory() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "kucing.jpg", "image/jpeg", "data".getBytes()
        );
        UUID recordId = UUID.randomUUID();

        String result = fileStorageService.storeFile(file, recordId);

        assertThat(result).endsWith(".jpg");
        assertThat(Files.exists(tempDir.resolve("uploads"))).isTrue();
    }

    // --- 2. Directory Already Exists ---
    // Mengcover: exists()=true
    @Test
    @DisplayName("Store File - Folder Sudah Ada (Skip Create)")
    void testStoreFile_DirectoryAlreadyExists() throws IOException {
        Path uploadPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadPath);

        MockMultipartFile file = new MockMultipartFile(
                "file", "anjing.jpg", "image/jpeg", "data".getBytes()
        );
        UUID recordId = UUID.randomUUID();

        String result = fileStorageService.storeFile(file, recordId);

        assertThat(result).endsWith(".jpg");
    }

    // --- 3. Filename Tanpa Titik (No Extension) ---
    // Mengcover: filename!=null (TRUE) && contains(".") (FALSE)
    @Test
    @DisplayName("Store File - Filename Tanpa Titik")
    void testStoreFile_NoExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "makefile", "text/plain", "data".getBytes()
        );
        UUID recordId = UUID.randomUUID();

        String result = fileStorageService.storeFile(file, recordId);

        // Ekstensi kosong, hanya prefix + uuid
        assertThat(result).startsWith("photo_").doesNotEndWith(".");
    }

    // --- 4. PERBAIKAN: Filename Null (Menggunakan Mockito) ---
    // Mengcover: filename!=null (FALSE) -> Branch 100% Hijau
    @Test
    @DisplayName("Store File - Filename Null (Real Null)")
    void testStoreFile_NullFilename() throws IOException {
        // PERBAIKAN: Jangan pakai new MockMultipartFile(..., null) karena akan diubah jadi ""
        // Gunakan mock() dari Mockito
        MultipartFile file = mock(MultipartFile.class);
        
        // Paksa return null murni
        when(file.getOriginalFilename()).thenReturn(null);
        
        // Kita juga perlu mock getInputStream agar tidak NullPointerException di baris 44 (Files.copy)
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        UUID recordId = UUID.randomUUID();

        // Act
        String result = fileStorageService.storeFile(file, recordId);

        // Assert: Pastikan tidak error dan nama file terbentuk tanpa ekstensi
        assertThat(result).startsWith("photo_");
    }

    // --- 5. Catch IOException ---
    @Test
    @DisplayName("Delete File - Simulasi Error IO")
    void testDeleteFile_IOException() {
        String filename = "error.jpg";

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Paksa deleteIfExists error
            filesMock.when(() -> Files.deleteIfExists(any(Path.class)))
                    .thenThrow(new IOException("Disk Error Simulation"));

            boolean result = fileStorageService.deleteFile(filename);

            assertThat(result).isFalse();
        }
    }

    // --- 6. Delete File Success ---
    @Test
    @DisplayName("Delete File - Success")
    void testDeleteFile_Success() throws IOException {
        Path uploadPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadPath);
        String filename = "sampah.txt";
        Files.createFile(uploadPath.resolve(filename));

        boolean result = fileStorageService.deleteFile(filename);

        assertThat(result).isTrue();
        assertThat(Files.exists(uploadPath.resolve(filename))).isFalse();
    }

    // --- 7. Load & Check Exists ---
    @Test
    @DisplayName("Load & Check File Exists")
    void testFileHelpers() throws IOException {
        Path uploadPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadPath);
        String filename = "test.png";
        Files.createFile(uploadPath.resolve(filename));

        assertThat(fileStorageService.fileExists(filename)).isTrue();
        assertThat(fileStorageService.loadFile(filename)).isNotNull();
    }
}