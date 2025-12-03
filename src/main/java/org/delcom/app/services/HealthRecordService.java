package org.delcom.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.HealthRecord;
import org.delcom.app.repositories.HealthRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthRecordService {
    
    // Ubah nama variabel repository agar sesuai konteks
    private final HealthRecordRepository healthRecordRepository;
    private final FileStorageService fileStorageService;

    public HealthRecordService(HealthRecordRepository healthRecordRepository, FileStorageService fileStorageService) {
        this.healthRecordRepository = healthRecordRepository;
        this.fileStorageService = fileStorageService;
    }

    // 1. Membuat Catatan Kesehatan Baru
    // -------------------------------
    @Transactional
    public HealthRecord createHealthRecord(UUID userId, HealthRecord reqRecord) {
        // Kita set userId di sini agar aman (dari token login)
        reqRecord.setUserId(userId);
        
        // Tanggal otomatis diatur di Entity (@PrePersist), tapi bisa diset manual jika perlu
        // reqRecord.setDate(LocalDate.now()); 

        return healthRecordRepository.save(reqRecord);
    }

    // 2. Mengambil Semua Catatan (dengan fitur pencarian)
    // -------------------------------
    public List<HealthRecord> getAllHealthRecords(UUID userId, String search) {
        if (search != null && !search.trim().isEmpty()) {
            // Mencari berdasarkan keyword di notes
            return healthRecordRepository.findByKeyword(userId, search);
        }
        // Mengambil semua data user
        return healthRecordRepository.findAllByUserId(userId);
    }

    // 3. Mengambil Satu Catatan berdasarkan ID
    // -------------------------------
    public HealthRecord getHealthRecordById(UUID userId, UUID id) {
        return healthRecordRepository.findByUserIdAndId(userId, id).orElse(null);
    }

    // 4. Update Data Kesehatan
    // -------------------------------
    @Transactional
    public HealthRecord updateHealthRecord(UUID userId, UUID id, HealthRecord reqRecord) {
        // Cari data lama
        HealthRecord existingRecord = healthRecordRepository.findByUserIdAndId(userId, id).orElse(null);
        
        if (existingRecord != null) {
            // Update field satu per satu
            existingRecord.setBodyTemperature(reqRecord.getBodyTemperature());
            existingRecord.setBloodPressure(reqRecord.getBloodPressure());
            existingRecord.setHeartRate(reqRecord.getHeartRate());
            existingRecord.setWaterIntake(reqRecord.getWaterIntake());
            existingRecord.setSleepDuration(reqRecord.getSleepDuration());
            existingRecord.setStressLevel(reqRecord.getStressLevel());
            existingRecord.setNotes(reqRecord.getNotes());
            
            // Jika tanggal diedit juga
            if(reqRecord.getDate() != null) {
                existingRecord.setDate(reqRecord.getDate());
            }

            return healthRecordRepository.save(existingRecord);
        }
        return null;
    }

    // 5. Hapus Catatan
    // -------------------------------
    @Transactional
    public boolean deleteHealthRecord(UUID userId, UUID id) {
        HealthRecord record = healthRecordRepository.findByUserIdAndId(userId, id).orElse(null);
        
        if (record == null) {
            return false;
        }

        // Jika ada foto, hapus fotonya juga dari penyimpanan
        if (record.getPhotoUrl() != null) {
            fileStorageService.deleteFile(record.getPhotoUrl());
        }

        healthRecordRepository.deleteById(id);
        return true;
    }

    // 6. Update Foto (Upload Gambar)
    // -------------------------------
    @Transactional
    public HealthRecord updatePhoto(UUID recordId, String photoFilename) {
        // Kita cari pakai ID saja (asumsi validasi user dilakukan di controller/sebelumnya)
        // Atau amannya pakai findById lalu cek userId di controller
        Optional<HealthRecord> recordOpt = healthRecordRepository.findById(recordId);
        
        if (recordOpt.isPresent()) {
            HealthRecord record = recordOpt.get();

            // Hapus file foto lama jika ada (biar server gak penuh sampah file)
            if (record.getPhotoUrl() != null) {
                fileStorageService.deleteFile(record.getPhotoUrl());
            }

            // Simpan nama file baru ke database
            record.setPhotoUrl(photoFilename);
            return healthRecordRepository.save(record);
        }
        return null;
    }
}
//