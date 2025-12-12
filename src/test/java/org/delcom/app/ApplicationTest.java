package org.delcom.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTest {

    @Test
    @DisplayName("Main Application Context Load (Sanity Check)")
    void contextLoads() {
        // Test ini memastikan Spring Context berhasil loading
    }

    // --- TAMBAHAN UNTUK MENGHIJAUKAN MAIN METHOD ---
    @Test
    @DisplayName("Test Main Method Execution")
    void testMain() {
        // Kita panggil method main secara manual agar JaCoCo mendeteksinya
        // Parameternya array kosong saja
        Application.main(new String[] {});
    }
}