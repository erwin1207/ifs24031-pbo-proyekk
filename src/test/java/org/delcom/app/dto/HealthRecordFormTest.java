package org.delcom.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

// Import AssertJ (Standar Spring Boot Test)
import static org.assertj.core.api.Assertions.assertThat;

class HealthRecordFormTest {

    @Test
    @DisplayName("Test Setters & Getters - Data Lengkap")
    void testSettersAndGetters() {
        // 1. Arrange (Siapkan Data)
        UUID mockId = UUID.randomUUID();
        Double bodyTemp = 36.5;
        String bloodPressure = "120/80";
        Integer heartRate = 72;
        Integer stressLevel = 3;
        String notes = "Kondisi stabil";
        Integer waterIntake = 2500;
        Double sleepDuration = 7.5;

        // 2. Act (Isi Object)
        HealthRecordForm form = new HealthRecordForm();
        form.setId(mockId);
        form.setBodyTemperature(bodyTemp);
        form.setBloodPressure(bloodPressure);
        form.setHeartRate(heartRate);
        form.setStressLevel(stressLevel);
        form.setNotes(notes);
        
        // Test field tambahan baru
        form.setWaterIntake(waterIntake);
        form.setSleepDuration(sleepDuration);

        // 3. Assert (Verifikasi Data)
        assertThat(form.getId()).isEqualTo(mockId);
        assertThat(form.getBodyTemperature()).isEqualTo(36.5);
        assertThat(form.getBloodPressure()).isEqualTo("120/80");
        assertThat(form.getHeartRate()).isEqualTo(72);
        assertThat(form.getStressLevel()).isEqualTo(3);
        assertThat(form.getNotes()).isEqualTo("Kondisi stabil");
        
        // Verifikasi field tambahan
        assertThat(form.getWaterIntake()).isEqualTo(2500);
        assertThat(form.getSleepDuration()).isEqualTo(7.5);
    }

    @Test
    @DisplayName("Test Null Handling - Field boleh null")
    void testNullValues() {
        // Karena tipe datanya Wrapper (Double, Integer) bukan primitif (double, int),
        // form ini harus bisa menerima nilai null (misal user tidak mengisi form lengkap).
        
        // 1. Arrange
        HealthRecordForm form = new HealthRecordForm();

        // 2. Act
        form.setBodyTemperature(null);
        form.setWaterIntake(null);
        form.setSleepDuration(null);

        // 3. Assert
        assertThat(form.getBodyTemperature()).isNull();
        assertThat(form.getWaterIntake()).isNull();
        assertThat(form.getSleepDuration()).isNull();
    }
    
    @Test
    @DisplayName("Test Constructor Kosong")
    void testConstructor() {
        // Memastikan constructor default ada dan object terbuat
        HealthRecordForm form = new HealthRecordForm();
        assertThat(form).isNotNull();
    }
}