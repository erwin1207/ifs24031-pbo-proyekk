package org.delcom.app.views;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import org.delcom.app.dto.HealthRecordPhotoForm; // DTO Foto
import org.delcom.app.dto.HealthRecordForm;      // DTO Input Data
import org.delcom.app.entities.HealthRecord;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.HealthRecordService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/health-records") // URL berubah dari /todos ke /health-records
public class HealthRecordView {

    private final HealthRecordService healthRecordService;
    private final FileStorageService fileStorageService;

    public HealthRecordView(HealthRecordService healthRecordService, FileStorageService fileStorageService) {
        this.healthRecordService = healthRecordService;
        this.fileStorageService = fileStorageService;
    }

    // 1. Tambah Catatan Kesehatan (Create)
    // ------------------------------------
    @PostMapping("/add")
    public String postAddRecord(@Valid @ModelAttribute("recordForm") HealthRecordForm recordForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Cek Login
        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/logout";

        // Validasi input (Suhu tubuh wajib diisi sebagai data minimal)
        if (recordForm.getBodyTemperature() == null) {
            redirectAttributes.addFlashAttribute("error", "Suhu tubuh wajib diisi");
            redirectAttributes.addFlashAttribute("addRecordModalOpen", true); // Trigger modal terbuka lagi
            return "redirect:/";
        }

        // Mapping dari DTO (Form) ke Entity
        HealthRecord newRecord = new HealthRecord();
        newRecord.setBodyTemperature(recordForm.getBodyTemperature());
        newRecord.setBloodPressure(recordForm.getBloodPressure());
        newRecord.setHeartRate(recordForm.getHeartRate());
        newRecord.setWaterIntake(recordForm.getWaterIntake()); // Asumsi di Form ada
        newRecord.setSleepDuration(recordForm.getSleepDuration()); // Asumsi di Form ada
        newRecord.setStressLevel(recordForm.getStressLevel());
        newRecord.setNotes(recordForm.getNotes());
        newRecord.setDate(LocalDate.now()); // Set tanggal hari ini

        // Simpan ke database via Service
        var entity = healthRecordService.createHealthRecord(authUser.getId(), newRecord);

        if (entity == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menyimpan catatan");
            redirectAttributes.addFlashAttribute("addRecordModalOpen", true);
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("success", "Catatan kesehatan berhasil ditambahkan.");
        return "redirect:/";
    }

    // 2. Edit Catatan Kesehatan (Update)
    // ----------------------------------
    @PostMapping("/edit")
    public String postEditRecord(@Valid @ModelAttribute("recordForm") HealthRecordForm recordForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {
        
        // Cek Login
        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/logout";

        // Validasi ID
        if (recordForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID catatan tidak valid");
            return "redirect:/";
        }

        // Mapping data update
        HealthRecord updateData = new HealthRecord();
        updateData.setBodyTemperature(recordForm.getBodyTemperature());
        updateData.setBloodPressure(recordForm.getBloodPressure());
        updateData.setHeartRate(recordForm.getHeartRate());
        updateData.setStressLevel(recordForm.getStressLevel());
        updateData.setNotes(recordForm.getNotes());
        // Tambahkan field lain jika perlu

        // Panggil Service Update
        var updated = healthRecordService.updateHealthRecord(
                authUser.getId(),
                recordForm.getId(),
                updateData);

        if (updated == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui catatan");
            redirectAttributes.addFlashAttribute("editRecordModalOpen", true);
            redirectAttributes.addFlashAttribute("editRecordModalId", recordForm.getId());
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("success", "Catatan berhasil diperbarui.");
        return "redirect:/";
    }

    // 3. Hapus Catatan (Delete)
    // -------------------------
    @PostMapping("/delete")
    public String postDeleteRecord(@Valid @ModelAttribute("recordForm") HealthRecordForm recordForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/logout";

        if (recordForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID tidak valid");
            return "redirect:/";
        }

        // Hapus via Service
        boolean deleted = healthRecordService.deleteHealthRecord(authUser.getId(), recordForm.getId());
        
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus atau data tidak ditemukan");
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("success", "Catatan berhasil dihapus.");
        return "redirect:/";
    }

    // 4. Halaman Detail Catatan
    // -------------------------
    @GetMapping("/{id}")
    public String getDetailRecord(@PathVariable UUID id, Model model) {
        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/logout";

        model.addAttribute("auth", authUser);

        // Ambil data detail
        HealthRecord record = healthRecordService.getHealthRecordById(authUser.getId(), id);
        if (record == null) {
            return "redirect:/";
        }
        model.addAttribute("record", record);

        // Siapkan Form Upload Foto
        HealthRecordPhotoForm photoForm = new HealthRecordPhotoForm();
        photoForm.setId(id);
        model.addAttribute("photoForm", photoForm);

        // Mengarah ke template HTML detail (sesuai ConstUtil)
        return ConstUtil.TEMPLATE_PAGES_HEALTH_DETAIL;
    }

    // 5. Upload Foto (Edit Photo)
    // ---------------------------
    @PostMapping("/edit-photo")
    public String postEditPhoto(@Valid @ModelAttribute("photoForm") HealthRecordPhotoForm photoForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        User authUser = getAuthUser();
        if (authUser == null) return "redirect:/auth/logout";

        // Validasi File Kosong
        if (photoForm.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "File foto tidak boleh kosong");
            return "redirect:/health-records/" + photoForm.getId();
        }

        // Cek Validitas Data
        HealthRecord record = healthRecordService.getHealthRecordById(authUser.getId(), photoForm.getId());
        if (record == null) {
            return "redirect:/";
        }

        // Validasi Tipe File (Gambar only)
        if (!photoForm.isValidImage()) {
            redirectAttributes.addFlashAttribute("error", "Format file harus JPG/PNG/GIF");
            return "redirect:/health-records/" + photoForm.getId();
        }

        // Validasi Ukuran (Max 5MB)
        if (!photoForm.isSizeValid(5 * 1024 * 1024)) {
            redirectAttributes.addFlashAttribute("error", "Ukuran file terlalu besar (Max 5MB)");
            return "redirect:/health-records/" + photoForm.getId();
        }

        try {
            // Simpan File
            String fileName = fileStorageService.storeFile(photoForm.getPhotoFile(), photoForm.getId());

            // Update database dengan nama file baru
            healthRecordService.updatePhoto(photoForm.getId(), fileName);

            redirectAttributes.addFlashAttribute("success", "Foto berhasil diupload");
            return "redirect:/health-records/" + photoForm.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengupload foto");
            return "redirect:/health-records/" + photoForm.getId();
        }
    }

    // 6. Serve File Foto (Agar bisa tampil di HTML)
    // ---------------------------------------------
    @GetMapping("/photo/{filename:.+}")
    @ResponseBody
    public Resource getPhotoByFilename(@PathVariable String filename) {
        try {
            Path file = fileStorageService.loadFile(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    // Helper: Mendapatkan User yang sedang login
    private User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return null;
        }
        return (User) principal;
    }
}