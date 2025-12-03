package org.delcom.app.dto;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;

public class HealthRecordPhotoForm {

    private UUID id; // ID dari HealthRecord yang akan diberi foto

    @NotNull(message = "Foto tidak boleh kosong")
    private MultipartFile photoFile; // Ubah nama variabel dari coverFile ke photoFile

    // Constructor
    public HealthRecordPhotoForm() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MultipartFile getPhotoFile() {
        return photoFile;
    }

    public void setPhotoFile(MultipartFile photoFile) {
        this.photoFile = photoFile;
    }

    // Helper methods
    public boolean isEmpty() {
        return photoFile == null || photoFile.isEmpty();
    }

    public String getOriginalFilename() {
        return photoFile != null ? photoFile.getOriginalFilename() : null;
    }

    // Validation methods
    public boolean isValidImage() {
        if (this.isEmpty()) {
            return false;
        }

        String contentType = photoFile.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp"));
    }

    public boolean isSizeValid(long maxSize) {
        return photoFile != null && photoFile.getSize() <= maxSize;
    }
}