package org.delcom.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    // Mengambil lokasi folder upload dari application.properties
    // Jika tidak ada settingan, default ke folder "./uploads"
    @Value("${app.upload.dir:./uploads}")
    protected String uploadDir;

    // Method untuk menyimpan file foto
    // Parameter kita ubah namanya dari todoId menjadi recordId agar lebih masuk akal
    public String storeFile(MultipartFile file, UUID recordId) throws IOException {
        // Buat directory jika belum ada
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Ambil nama file asli & ekstensinya (misal: .jpg, .png)
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate nama file baru yang unik
        // Ubah prefix "cover_" menjadi "photo_" agar sesuai konteks kesehatan
        String filename = "photo_" + recordId.toString() + fileExtension;

        // Simpan file ke folder tujuan (overwrite jika ada file dengan nama sama)
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    // Menghapus file (dipakai saat menghapus catatan kesehatan)
    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    // Mengambil path file (untuk ditampilkan di Controller nanti)
    public Path loadFile(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    // Cek apakah file ada
    public boolean fileExists(String filename) {
        return Files.exists(loadFile(filename));
    }
}