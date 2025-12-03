package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, UUID> {
    
    // 1. Mencari catatan berdasarkan kata kunci (Pencarian di kolom 'notes')
    // Kita ubah 'Todo t' menjadi 'HealthRecord h'
    // Kita ubah pencarian 'title/description' menjadi 'notes'
    @Query("SELECT h FROM HealthRecord h WHERE (LOWER(h.notes) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND h.userId = :userId ORDER BY h.date DESC, h.createdAt DESC")
    List<HealthRecord> findByKeyword(UUID userId, String keyword);

    // 2. Mengambil semua catatan milik user tertentu
    // Diurutkan berdasarkan tanggal pencatatan (date) terbaru
    @Query("SELECT h FROM HealthRecord h WHERE h.userId = :userId ORDER BY h.date DESC, h.createdAt DESC")
    List<HealthRecord> findAllByUserId(UUID userId);

    // 3. Mengambil satu catatan spesifik milik user (untuk keamanan, biar orang lain gak bisa baca)
    @Query("SELECT h FROM HealthRecord h WHERE h.id = :id AND h.userId = :userId")
    Optional<HealthRecord> findByUserIdAndId(UUID userId, UUID id);
}