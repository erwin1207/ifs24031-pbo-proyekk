package org.delcom.app.services;

import org.delcom.app.entities.HealthRecord;
import org.delcom.app.repositories.HealthRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Aktifkan Mockito (Cepat & Ringan)
class HealthRecordServiceTest {

    @Mock
    private HealthRecordRepository healthRecordRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private HealthRecordService healthRecordService;

    // --- 1. TEST CREATE ---

    @Test
    @DisplayName("Create Record - Simpan ke Repository")
    void testCreateHealthRecord() {
        // 1. Arrange
        UUID userId = UUID.randomUUID();
        HealthRecord reqRecord = new HealthRecord();
        reqRecord.setNotes("Baru");

        // Mock repository save me-return object yang sama
        when(healthRecordRepository.save(any(HealthRecord.class))).thenReturn(reqRecord);

        // 2. Act
        HealthRecord result = healthRecordService.createHealthRecord(userId, reqRecord);

        // 3. Assert
        assertThat(result).isNotNull();
        // Pastikan userId diset oleh service
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(healthRecordRepository).save(reqRecord);
    }

    // --- 2. TEST GET ALL (SEARCH LOGIC) ---

    @Test
    @DisplayName("Get All - Tanpa Search (Find All By User)")
    void testGetAll_NoSearch() {
        // 1. Arrange
        UUID userId = UUID.randomUUID();
        when(healthRecordRepository.findAllByUserId(userId))
                .thenReturn(Arrays.asList(new HealthRecord()));

        // 2. Act (Search null atau kosong)
        List<HealthRecord> result = healthRecordService.getAllHealthRecords(userId, null);

        // 3. Assert
        assertThat(result).hasSize(1);
        verify(healthRecordRepository).findAllByUserId(userId);
        // Pastikan TIDAK memanggil findByKeyword
        verify(healthRecordRepository, never()).findByKeyword(any(), any());
    }
    
    // TAMBAHAN: Untuk menutup Kuning di Baris 40 (Logic Search Empty)
    @Test
    @DisplayName("Get All - Search String Kosong (Find All By User)")
    void testGetAll_SearchEmptyString() {
        UUID userId = UUID.randomUUID();
        // Search hanya spasi
        healthRecordService.getAllHealthRecords(userId, "   ");
        
        verify(healthRecordRepository).findAllByUserId(userId);
        verify(healthRecordRepository, never()).findByKeyword(any(), any());
    }

    @Test
    @DisplayName("Get All - Dengan Search (Find By Keyword)")
    void testGetAll_WithSearch() {
        // 1. Arrange
        UUID userId = UUID.randomUUID();
        String keyword = "demam";
        when(healthRecordRepository.findByKeyword(userId, keyword))
                .thenReturn(Arrays.asList(new HealthRecord()));

        // 2. Act
        List<HealthRecord> result = healthRecordService.getAllHealthRecords(userId, keyword);

        // 3. Assert
        assertThat(result).hasSize(1);
        verify(healthRecordRepository).findByKeyword(userId, keyword);
        verify(healthRecordRepository, never()).findAllByUserId(any());
    }

    // --- 3. TEST GET BY ID ---

    @Test
    @DisplayName("Get By ID - Found vs Not Found")
    void testGetById() {
        UUID uid = UUID.randomUUID();
        UUID rid = UUID.randomUUID();
        HealthRecord rec = new HealthRecord();

        // Case Found
        when(healthRecordRepository.findByUserIdAndId(uid, rid)).thenReturn(Optional.of(rec));
        assertThat(healthRecordService.getHealthRecordById(uid, rid)).isNotNull();

        // Case Not Found
        when(healthRecordRepository.findByUserIdAndId(uid, rid)).thenReturn(Optional.empty());
        assertThat(healthRecordService.getHealthRecordById(uid, rid)).isNull();
    }

    // --- 4. TEST UPDATE ---

    @Test
    @DisplayName("Update Record - Sukses Update Fields")
    void testUpdateHealthRecord() {
        // 1. Arrange
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();

        // Data Lama di DB
        HealthRecord existing = new HealthRecord();
        existing.setId(recordId);
        existing.setBodyTemperature(36.0);
        existing.setNotes("Lama");
        existing.setDate(LocalDate.of(2023, 1, 1)); // Tanggal lama

        // Data Baru dari User
        HealthRecord req = new HealthRecord();
        req.setBodyTemperature(38.5); // Demam
        req.setNotes("Baru Update");
        req.setDate(LocalDate.now()); // Tanggal baru

        when(healthRecordRepository.findByUserIdAndId(userId, recordId))
                .thenReturn(Optional.of(existing));
        when(healthRecordRepository.save(existing)).thenReturn(existing);

        // 2. Act
        HealthRecord updated = healthRecordService.updateHealthRecord(userId, recordId, req);

        // 3. Assert
        assertThat(updated).isNotNull();
        // Cek apakah field berubah sesuai input
        assertThat(updated.getBodyTemperature()).isEqualTo(38.5);
        assertThat(updated.getNotes()).isEqualTo("Baru Update");
        assertThat(updated.getDate()).isEqualTo(LocalDate.now());
        
        verify(healthRecordRepository).save(existing);
    }
    
    // TAMBAHAN: Untuk menutup Kuning di Baris 72 (Tanggal Input Null)
    @Test
    @DisplayName("Update Record - Input Tanggal Null (Jangan Timpa Tanggal Lama)")
    void testUpdateHealthRecord_DateNull() {
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();

        HealthRecord existing = new HealthRecord();
        LocalDate oldDate = LocalDate.of(2023, 1, 1);
        existing.setDate(oldDate);

        HealthRecord req = new HealthRecord();
        req.setDate(null); // Input user tidak ada tanggal

        when(healthRecordRepository.findByUserIdAndId(userId, recordId))
                .thenReturn(Optional.of(existing));
        when(healthRecordRepository.save(existing)).thenReturn(existing);

        HealthRecord updated = healthRecordService.updateHealthRecord(userId, recordId, req);

        // Pastikan tanggal lama TIDAK berubah
        assertThat(updated.getDate()).isEqualTo(oldDate);
    }

    @Test
    @DisplayName("Update Record - Data Tidak Ditemukan")
    void testUpdateHealthRecord_NotFound() {
        when(healthRecordRepository.findByUserIdAndId(any(), any())).thenReturn(Optional.empty());

        HealthRecord result = healthRecordService.updateHealthRecord(UUID.randomUUID(), UUID.randomUUID(), new HealthRecord());
        
        assertThat(result).isNull(); // Menutup Kuning di Baris 61 (Else Branch)
        verify(healthRecordRepository, never()).save(any());
    }

    // --- 5. TEST DELETE (WITH FILE CLEANUP) ---

    @Test
    @DisplayName("Delete Record - Hapus File & Data DB")
    void testDeleteHealthRecord_WithPhoto() {
        // 1. Arrange
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        
        HealthRecord record = new HealthRecord();
        record.setPhotoUrl("foto_kesehatan.jpg"); // Punya foto

        when(healthRecordRepository.findByUserIdAndId(userId, recordId))
                .thenReturn(Optional.of(record));

        // 2. Act
        boolean isDeleted = healthRecordService.deleteHealthRecord(userId, recordId);

        // 3. Assert
        assertThat(isDeleted).isTrue();
        
        // PENTING: Pastikan service menyuruh fileStorageService menghapus foto
        verify(fileStorageService).deleteFile("foto_kesehatan.jpg");
        
        // Pastikan data DB dihapus
        verify(healthRecordRepository).deleteById(recordId);
    }
    
    // TAMBAHAN: Untuk menutup Kuning di Baris 92 (Record Tanpa Foto)
    @Test
    @DisplayName("Delete Record - Tanpa Foto (Skip Delete File)")
    void testDeleteHealthRecord_NoPhoto() {
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        
        HealthRecord record = new HealthRecord();
        record.setPhotoUrl(null); // TIDAK ADA FOTO

        when(healthRecordRepository.findByUserIdAndId(userId, recordId))
                .thenReturn(Optional.of(record));

        boolean isDeleted = healthRecordService.deleteHealthRecord(userId, recordId);

        assertThat(isDeleted).isTrue();
        // Pastikan deleteFile TIDAK DIPANGGIL
        verify(fileStorageService, never()).deleteFile(any());
        verify(healthRecordRepository).deleteById(recordId);
    }

    @Test
    @DisplayName("Delete Record - Tidak Ditemukan")
    void testDeleteHealthRecord_NotFound() {
        when(healthRecordRepository.findByUserIdAndId(any(), any())).thenReturn(Optional.empty());
        
        boolean isDeleted = healthRecordService.deleteHealthRecord(UUID.randomUUID(), UUID.randomUUID());
        
        assertThat(isDeleted).isFalse();
        verify(fileStorageService, never()).deleteFile(any());
        verify(healthRecordRepository, never()).deleteById(any());
    }

    // --- 6. TEST UPDATE PHOTO ---

    @Test
    @DisplayName("Update Photo - Hapus Lama, Set Baru")
    void testUpdatePhoto() {
        // 1. Arrange
        UUID recordId = UUID.randomUUID();
        String oldPhoto = "lama.jpg";
        String newPhoto = "baru.jpg";

        HealthRecord record = new HealthRecord();
        record.setId(recordId);
        record.setPhotoUrl(oldPhoto);

        when(healthRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(healthRecordRepository.save(record)).thenReturn(record);

        // 2. Act
        HealthRecord result = healthRecordService.updatePhoto(recordId, newPhoto);

        // 3. Assert
        assertThat(result).isNotNull();
        assertThat(result.getPhotoUrl()).isEqualTo(newPhoto);

        // Pastikan foto lama dihapus
        verify(fileStorageService).deleteFile(oldPhoto);
        // Pastikan disimpan
        verify(healthRecordRepository).save(record);
    }
    
    // TAMBAHAN: Untuk menutup Merah di Baris 120 (Record Not Found saat Update Foto)
    @Test
    @DisplayName("Update Photo - Record Tidak Ditemukan")
    void testUpdatePhoto_NotFound() {
        UUID recordId = UUID.randomUUID();
        
        // Mock DB return empty
        when(healthRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        HealthRecord result = healthRecordService.updatePhoto(recordId, "any.jpg");

        // Code harus lari ke baris 120 (return null)
        assertThat(result).isNull();
    }
    
    // TAMBAHAN: Untuk menutup Kuning di Baris 112 (Record Tanpa Foto Lama)
    @Test
    @DisplayName("Update Photo - Belum Ada Foto Lama (Skip Delete)")
    void testUpdatePhoto_NoOldPhoto() {
        UUID recordId = UUID.randomUUID();
        String newPhoto = "baru.jpg";

        HealthRecord record = new HealthRecord();
        record.setPhotoUrl(null); // Foto lama kosong

        when(healthRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(healthRecordRepository.save(record)).thenAnswer(i -> i.getArguments()[0]);

        HealthRecord result = healthRecordService.updatePhoto(recordId, newPhoto);

        // Pastikan deleteFile TIDAK dipanggil
        verify(fileStorageService, never()).deleteFile(anyString());
        // Tapi foto baru tetap diset
        assertThat(result.getPhotoUrl()).isEqualTo(newPhoto);
    }
}