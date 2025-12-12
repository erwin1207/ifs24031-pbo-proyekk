package org.delcom.app.dto;

import org.springframework.web.multipart.MultipartFile;

public class UpdateProfileForm {
    private String namaLengkap;
    private MultipartFile fotoProfil;

    // Getter dan Setter
    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public MultipartFile getFotoProfil() {
        return fotoProfil;
    }

    public void setFotoProfil(MultipartFile fotoProfil) {
        this.fotoProfil = fotoProfil;
    }
}