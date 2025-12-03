package org.delcom.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.HealthRecord;
import org.delcom.app.entities.User;
import org.delcom.app.services.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {
    
    // Kita ubah service-nya dari TodoService ke HealthRecordService
    private final HealthRecordService healthRecordService;

    @Autowired
    protected AuthContext authContext;

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    // Menambahkan catatan kesehatan baru
    // -------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, UUID>>> createRecord(@RequestBody HealthRecord reqRecord) {

        // Validasi input: Kita cek apakah data penting (Suhu & Tensi) sudah diisi
        if (reqRecord.getBodyTemperature() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Suhu tubuh harus diisi", null));
        } else if (reqRecord.getBloodPressure() == null || reqRecord.getBloodPressure().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Tekanan darah harus diisi", null));
        } else if (reqRecord.getHeartRate() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Detak jantung harus diisi", null));
        }

        // Validasi autentikasi (Sama persis seperti TodoController)
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        // Memanggil service untuk create data
        HealthRecord newRecord = healthRecordService.createHealthRecord(authUser.getId(), reqRecord);
        
        return ResponseEntity.ok(new ApiResponse<Map<String, UUID>>(
                "success",
                "Catatan kesehatan berhasil dibuat",
                Map.of("id", newRecord.getId())));
    }

    // Mendapatkan semua catatan dengan opsi pencarian (search notes/tanggal)
    // -------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<HealthRecord>>>> getAllRecords(
            @RequestParam(required = false) String search) {
        
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<HealthRecord> records = healthRecordService.getAllHealthRecords(authUser.getId(), search);
        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Daftar catatan kesehatan berhasil diambil",
                Map.of("records", records)));
    }

    // Mendapatkan catatan berdasarkan ID
    // -------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, HealthRecord>>> getRecordById(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        HealthRecord record = healthRecordService.getHealthRecordById(authUser.getId(), id);
        if (record == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data catatan tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data catatan berhasil diambil",
                Map.of("record", record)));
    }

    // Memperbarui catatan berdasarkan ID
    // -------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HealthRecord>> updateRecord(@PathVariable UUID id, @RequestBody HealthRecord reqRecord) {

        // Validasi input saat update
        if (reqRecord.getBodyTemperature() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Suhu tubuh tidak valid", null));
        } 
        // Boleh tambahkan validasi lain sesuai kebutuhan

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        HealthRecord updatedRecord = healthRecordService.updateHealthRecord(authUser.getId(), id, reqRecord);
        
        if (updatedRecord == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data catatan tidak ditemukan atau akses ditolak", null));
        }

        return ResponseEntity.ok(new ApiResponse<>("success", "Data catatan berhasil diperbarui", null));
    }

    // Menghapus catatan berdasarkan ID
    // -------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRecord(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403).body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        boolean status = healthRecordService.deleteHealthRecord(authUser.getId(), id);
        if (!status) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Data catatan tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data catatan berhasil dihapus",
                null));
    }
}