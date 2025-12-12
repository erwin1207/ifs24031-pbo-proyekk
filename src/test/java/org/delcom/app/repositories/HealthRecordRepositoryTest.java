package org.delcom.app.repositories;

import org.delcom.app.entities.HealthRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Menggunakan H2 Database in-memory
class HealthRecordRepositoryTest {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Test Find All By UserId - Filtering & Sorting")
    void testFindAllByUserId() {
        // 1. Arrange
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        // Record User A (Tanggal Kemarin)
        HealthRecord recordA1 = new HealthRecord();
        recordA1.setUserId(userA);
        recordA1.setDate(LocalDate.now().minusDays(1));
        recordA1.setNotes("Catatan Kemarin");
        entityManager.persist(recordA1);

        // Record User A (Tanggal Hari Ini) - Harusnya muncul pertama (DESC)
        HealthRecord recordA2 = new HealthRecord();
        recordA2.setUserId(userA);
        recordA2.setDate(LocalDate.now());
        recordA2.setNotes("Catatan Hari Ini");
        entityManager.persist(recordA2);

        // Record User B (Jangan sampai ikut terambil)
        HealthRecord recordB = new HealthRecord();
        recordB.setUserId(userB);
        recordB.setDate(LocalDate.now());
        recordB.setNotes("Punya orang lain");
        entityManager.persist(recordB);

        entityManager.flush(); // Tulis ke DB

        // 2. Act
        List<HealthRecord> results = healthRecordRepository.findAllByUserId(userA);

        // 3. Assert
        assertThat(results).hasSize(2); // Hanya punya User A
        
        // Cek Urutan: Index 0 harusnya Hari Ini (Date Terbaru)
        assertThat(results.get(0).getNotes()).isEqualTo("Catatan Hari Ini");
        assertThat(results.get(1).getNotes()).isEqualTo("Catatan Kemarin");
    }

    @Test
    @DisplayName("Test Find By Keyword - Case Insensitive Search")
    void testFindByKeyword() {
        // 1. Arrange
        UUID userA = UUID.randomUUID();

        // Record 1: Ada kata "Demam"
        HealthRecord r1 = new HealthRecord();
        r1.setUserId(userA);
        r1.setNotes("Anak sedang demam tinggi");
        entityManager.persist(r1);

        // Record 2: Ada kata "Pusing"
        HealthRecord r2 = new HealthRecord();
        r2.setUserId(userA);
        r2.setNotes("Kepala pusing sekali");
        entityManager.persist(r2);

        // Record 3: Ada kata "Demam" TAPI punya User Lain
        HealthRecord r3 = new HealthRecord();
        r3.setUserId(UUID.randomUUID());
        r3.setNotes("Juga demam");
        entityManager.persist(r3);

        entityManager.flush();

        // 2. Act (Cari "DEMAM" huruf besar semua)
        List<HealthRecord> results = healthRecordRepository.findByKeyword(userA, "DEMAM");

        // 3. Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getNotes()).isEqualTo("Anak sedang demam tinggi");
        
        // Pastikan record pusing tidak terbawa
        // Pastikan record user lain tidak terbawa
    }

    @Test
    @DisplayName("Test Find By Keyword - Keyword Kosong (Semua Data)")
    void testFindByEmptyKeyword() {
        // Jika keyword kosong string "", query LIKE '%%' akan match semua row
        // 1. Arrange
        UUID userId = UUID.randomUUID();
        HealthRecord r1 = new HealthRecord(); r1.setUserId(userId); r1.setNotes("A");
        HealthRecord r2 = new HealthRecord(); r2.setUserId(userId); r2.setNotes("B");
        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.flush();

        // 2. Act
        List<HealthRecord> results = healthRecordRepository.findByKeyword(userId, "");

        // 3. Assert
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("Test Find By UserId And Id - Security Check")
    void testFindByUserIdAndId() {
        // 1. Arrange
        UUID ownerId = UUID.randomUUID();
        UUID hackerId = UUID.randomUUID();

        HealthRecord record = new HealthRecord();
        record.setUserId(ownerId);
        record.setNotes("Rahasia Pribadi");
        
        HealthRecord savedRecord = entityManager.persistAndFlush(record);
        UUID recordId = savedRecord.getId();

        // 2. Act & Assert
        
        // Skenario 1: Owner mengakses -> Ketemu
        Optional<HealthRecord> foundOwner = healthRecordRepository.findByUserIdAndId(ownerId, recordId);
        assertThat(foundOwner).isPresent();
        assertThat(foundOwner.get().getNotes()).isEqualTo("Rahasia Pribadi");

        // Skenario 2: Hacker mengakses data orang lain -> Tidak Ketemu (Empty)
        Optional<HealthRecord> foundHacker = healthRecordRepository.findByUserIdAndId(hackerId, recordId);
        assertThat(foundHacker).isEmpty();
    }
}