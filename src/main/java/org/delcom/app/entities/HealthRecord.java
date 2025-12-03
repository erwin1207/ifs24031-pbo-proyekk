package org.delcom.app.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_records") // Nama tabel di database berubah
public class HealthRecord {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // --- Atribut Kesehatan Baru ---

    @Column(name = "date", nullable = false)
    private LocalDate date; // Tanggal pencatatan (misal: data tgl 1 Jan, meski diinput tgl 2 Jan)

    @Column(name = "body_temperature")
    private Double bodyTemperature; // Suhu Tubuh (ex: 36.5)

    @Column(name = "blood_pressure")
    private String bloodPressure;   // Tekanan Darah (ex: "120/80")

    @Column(name = "heart_rate")
    private Integer heartRate;      // Detak Jantung (ex: 80)

    @Column(name = "water_intake")
    private Integer waterIntake;    // Minum Air (ex: 8 gelas)

    @Column(name = "sleep_duration")
    private Double sleepDuration;   // Tidur (ex: 7.5 jam)

    @Column(name = "stress_level")
    private Integer stressLevel;    // Stress (1-10)

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;           // Catatan Tambahan (pengganti description)

    @Column(name = "photo_url")
    private String photoUrl;        // Foto pendukung (pengganti cover)

    // --- Timestamp Standar ---

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Constructors ---
    
    public HealthRecord() {
    }

    public HealthRecord(UUID userId, LocalDate date, Double bodyTemperature, String bloodPressure, Integer heartRate, String notes) {
        this.userId = userId;
        this.date = date;
        this.bodyTemperature = bodyTemperature;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.notes = notes;
    }

    // --- Getters & Setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getBodyTemperature() { return bodyTemperature; }
    public void setBodyTemperature(Double bodyTemperature) { this.bodyTemperature = bodyTemperature; }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Integer getWaterIntake() { return waterIntake; }
    public void setWaterIntake(Integer waterIntake) { this.waterIntake = waterIntake; }

    public Double getSleepDuration() { return sleepDuration; }
    public void setSleepDuration(Double sleepDuration) { this.sleepDuration = sleepDuration; }

    public Integer getStressLevel() { return stressLevel; }
    public void setStressLevel(Integer stressLevel) { this.stressLevel = stressLevel; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ======= @PrePersist & @PreUpdate =======
    @PrePersist
    protected void onCreate() {
        if (this.date == null) {
            this.date = LocalDate.now(); // Default ke hari ini jika tanggal kosong
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}