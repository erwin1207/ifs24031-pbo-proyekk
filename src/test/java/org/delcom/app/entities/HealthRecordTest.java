package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Load JPA & H2 Database
class HealthRecordTest {

    @Autowired
    private TestEntityManager entityManager;

    // --- 1. TAMBAHAN PENTING: Fix Merah pada Constructor (Baris 63-70) ---
    @Test
    @DisplayName("Test All-Args Constructor & Getters (Coverage Fix)")
    void testConstructorAndGetters() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2023, 10, 10);
        Double temp = 36.5;
        String bp = "120/80";
        Integer hr = 80;
        String notes = "Sehat";

        // Act: Panggil Constructor Lengkap (Ini yang sebelumnya MERAH)
        HealthRecord record = new HealthRecord(userId, date, temp, bp, hr, notes);

        // Assert: Pastikan nilai masuk dengan benar (Fix Coverage Getters)
        assertThat(record.getUserId()).isEqualTo(userId);
        assertThat(record.getDate()).isEqualTo(date);
        assertThat(record.getBodyTemperature()).isEqualTo(temp);
        assertThat(record.getBloodPressure()).isEqualTo(bp);
        assertThat(record.getHeartRate()).isEqualTo(hr);
        assertThat(record.getNotes()).isEqualTo(notes);
    }

    // --- 2. Test JPA: Save Record (Cover Baris 113 - False Branch) ---
    @Test
    @DisplayName("Test Save Record - Date Manual (Skip Default Date Logic)")
    void testSaveRecord() {
        // 1. Arrange
        UUID userId = UUID.randomUUID();
        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setBodyTemperature(36.6);
        record.setBloodPressure("110/70");
        record.setHeartRate(75);
        record.setNotes("Checkup rutin");
        
        // Kita set tanggal manual -> Ini membuat logic 'if (date == null)' menjadi FALSE
        record.setDate(LocalDate.of(2023, 1, 1)); 

        // 2. Act
        HealthRecord savedRecord = entityManager.persistAndFlush(record);

        // 3. Assert
        assertThat(savedRecord.getId()).isNotNull();
        assertThat(savedRecord.getUserId()).isEqualTo(userId);
        assertThat(savedRecord.getBodyTemperature()).isEqualTo(36.6);
        assertThat(savedRecord.getDate()).isEqualTo(LocalDate.of(2023, 1, 1)); // Tanggal tidak berubah
        
        assertThat(savedRecord.getCreatedAt()).isNotNull();
        assertThat(savedRecord.getUpdatedAt()).isNotNull();
    }

    // --- 3. Test JPA: Default Date (Cover Baris 113 - True Branch) ---
    @Test
    @DisplayName("Test Default Date Logic - Date Null (Auto Set Now)")
    void testDefaultDateLogic() {
        // 1. Arrange
        HealthRecord record = new HealthRecord();
        record.setUserId(UUID.randomUUID());
        // Date dibiarkan NULL -> Ini membuat logic 'if (date == null)' menjadi TRUE

        // 2. Act
        HealthRecord savedRecord = entityManager.persistAndFlush(record);

        // 3. Assert
        assertThat(savedRecord.getDate()).isNotNull();
        assertThat(savedRecord.getDate()).isEqualTo(LocalDate.now()); // Otomatis hari ini
        assertThat(savedRecord.getCreatedAt()).isNotNull();
    }

    // --- 4. Test JPA: Update Logic ---
    @Test
    @DisplayName("Test Update Logic (@PreUpdate)")
    void testUpdateLogic() {
        // 1. Arrange & Save awal
        HealthRecord record = new HealthRecord();
        record.setUserId(UUID.randomUUID());
        record.setNotes("Awal");
        
        HealthRecord savedRecord = entityManager.persistAndFlush(record);
        
        // Force flush & clear agar objek didetach dari persistence context
        entityManager.flush(); 
        entityManager.clear(); 

        // 2. Act (Update Data)
        HealthRecord foundRecord = entityManager.find(HealthRecord.class, savedRecord.getId());
        foundRecord.setNotes("Revisi");
        
        // Simpan perubahan
        HealthRecord updatedRecord = entityManager.persistAndFlush(foundRecord);

        // 3. Assert
        assertThat(updatedRecord.getNotes()).isEqualTo("Revisi");
        assertThat(updatedRecord.getUpdatedAt()).isNotNull();
    }

    // --- 5. Test Optional Fields ---
    @Test
    @DisplayName("Test Optional Fields (Setters & Getters)")
    void testOptionalFields() {
        // 1. Arrange
        HealthRecord record = new HealthRecord();
        record.setUserId(UUID.randomUUID());
        
        // Isi field optional
        record.setWaterIntake(8);
        record.setSleepDuration(7.5);
        record.setStressLevel(5);
        record.setPhotoUrl("/images/bukti.jpg");

        // 2. Act
        HealthRecord savedRecord = entityManager.persistAndFlush(record);

        // 3. Assert
        assertThat(savedRecord.getWaterIntake()).isEqualTo(8);
        assertThat(savedRecord.getSleepDuration()).isEqualTo(7.5);
        assertThat(savedRecord.getStressLevel()).isEqualTo(5);
        assertThat(savedRecord.getPhotoUrl()).isEqualTo("/images/bukti.jpg");
    }
}