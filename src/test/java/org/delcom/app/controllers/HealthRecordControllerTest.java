package org.delcom.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.HealthRecord;
import org.delcom.app.entities.User;
import org.delcom.app.interceptors.AuthInterceptor;
import org.delcom.app.services.HealthRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthRecordController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class,
    OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class HealthRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthRecordService healthRecordService;

    // --- MOCK INTERCEPTOR AGAR TIDAK 401 ---
    @MockBean
    private AuthInterceptor authInterceptor;
    // ---------------------------------------

    @Autowired
    private AuthContext authContext;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private HealthRecord mockHealthRecord;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AuthContext authContext() {
            return mock(AuthContext.class);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // 1. Setup User
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("Tester");
        mockUser.setEmail("test@delcom.org");

        // 2. Setup HealthRecord
        mockHealthRecord = new HealthRecord();
        mockHealthRecord.setId(UUID.randomUUID());
        mockHealthRecord.setUserId(mockUser.getId());
        mockHealthRecord.setBodyTemperature(36.5);
        mockHealthRecord.setBloodPressure("120/80");
        mockHealthRecord.setHeartRate(75);
        mockHealthRecord.setNotes("Kondisi sehat");

        // 3. Setup AuthContext (Login Session)
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);

        // 4. BYPASS INTERCEPTOR: Selalu return TRUE (Lolos)
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("Create Record - Sukses")
    void testCreateRecord_Success() throws Exception {
        HealthRecord reqRecord = new HealthRecord();
        reqRecord.setBodyTemperature(37.0);
        reqRecord.setBloodPressure("130/85");
        reqRecord.setHeartRate(80);
        reqRecord.setNotes("Sedikit demam");

        when(healthRecordService.createHealthRecord(any(UUID.class), any(HealthRecord.class)))
                .thenReturn(mockHealthRecord);

        mockMvc.perform(post("/api/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqRecord)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Catatan kesehatan berhasil dibuat"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @DisplayName("Create Record - Sukses (Minimal Data)")
    void testCreateRecord_Success_MinimalData() throws Exception {
        HealthRecord reqRecord = new HealthRecord();
        reqRecord.setBodyTemperature(36.5);
        reqRecord.setBloodPressure("120/80");
        reqRecord.setHeartRate(72);
        // Notes tidak diisi (optional)

        when(healthRecordService.createHealthRecord(any(UUID.class), any(HealthRecord.class)))
                .thenReturn(mockHealthRecord);

        mockMvc.perform(post("/api/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqRecord)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @DisplayName("Create Record - Gagal (Suhu tubuh kosong)")
    void testCreateRecord_Fail_MissingBodyTemperature() throws Exception {
        HealthRecord reqRecord = new HealthRecord();
        reqRecord.setBloodPressure("130/85");
        reqRecord.setHeartRate(80);

        mockMvc.perform(post("/api/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqRecord)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Suhu tubuh harus diisi"));
    }

    @Test
    @DisplayName("Create Record - Gagal (Tekanan darah kosong)")
    void testCreateRecord_Fail_MissingBloodPressure() throws Exception {
        HealthRecord reqRecord = new HealthRecord();
        reqRecord.setBodyTemperature(37.0);
        reqRecord.setHeartRate(80);

        mockMvc.perform(post("/api/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqRecord)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Tekanan darah harus diisi"));
    }

    @Test
    @DisplayName("Create Record - Gagal (Tekanan darah string kosong)")
    void testCreateRecord_Fail_EmptyBloodPressure() throws Exception {
        HealthRecord reqRecord = new HealthRecord();
        reqRecord.setBodyTemperature(37.0);
        reqRecord.setBloodPressure(""); // String kosong
        reqRecord.setHeartRate(80);

        mockMvc.perform(post("/api/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqRecord)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Tekanan darah harus diisi"));
    }

    @Test
    @DisplayName("Create Record - Gagal (Detak jantung kosong)")
    void testCreateRecord_Fail_MissingHeartRate() throws Exception {
        HealthRecord reqRecord = new HealthRecord();
        reqRecord.setBodyTemperature(37.0);
        reqRecord.setBloodPressure("130/85");

        mockMvc.perform(post("/api/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqRecord)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Detak jantung harus diisi"));
    }

    @Test
    @DisplayName("Create Record - Unauthorized")
    void testCreateRecord_Unauthorized() throws Exception {
        // Override setup: Matikan auth context
        when(authContext.isAuthenticated()).thenReturn(false);

        HealthRecord reqRecord = new HealthRecord();
        reqRecord.setBodyTemperature(37.0);
        reqRecord.setBloodPressure("130/85");
        reqRecord.setHeartRate(80);

        mockMvc.perform(post("/api/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqRecord)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("User tidak terautentikasi"));
    }

    @Test
    @DisplayName("Get All Records - Sukses")
    void testGetAllRecords_Success() throws Exception {
        List<HealthRecord> records = new ArrayList<>();
        records.add(mockHealthRecord);

        when(healthRecordService.getAllHealthRecords(any(UUID.class), eq(null)))
                .thenReturn(records);

        mockMvc.perform(get("/api/health-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Daftar catatan kesehatan berhasil diambil"))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("Get All Records - Sukses (Empty List)")
    void testGetAllRecords_Success_EmptyList() throws Exception {
        List<HealthRecord> records = new ArrayList<>();

        when(healthRecordService.getAllHealthRecords(any(UUID.class), eq(null)))
                .thenReturn(records);

        mockMvc.perform(get("/api/health-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    @DisplayName("Get All Records - Dengan Search")
    void testGetAllRecords_WithSearch() throws Exception {
        List<HealthRecord> records = new ArrayList<>();
        records.add(mockHealthRecord);

        when(healthRecordService.getAllHealthRecords(any(UUID.class), eq("demam")))
                .thenReturn(records);

        mockMvc.perform(get("/api/health-records")
                .param("search", "demam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Get All Records - Dengan Search (No Results)")
    void testGetAllRecords_WithSearch_NoResults() throws Exception {
        List<HealthRecord> records = new ArrayList<>();

        when(healthRecordService.getAllHealthRecords(any(UUID.class), eq("xyz")))
                .thenReturn(records);

        mockMvc.perform(get("/api/health-records")
                .param("search", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    @DisplayName("Get All Records - Dengan Search Empty String")
    void testGetAllRecords_WithSearchEmptyString() throws Exception {
        List<HealthRecord> records = new ArrayList<>();
        records.add(mockHealthRecord);

        when(healthRecordService.getAllHealthRecords(any(UUID.class), eq("")))
                .thenReturn(records);

        mockMvc.perform(get("/api/health-records")
                .param("search", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Get All Records - Unauthorized")
    void testGetAllRecords_Unauthorized() throws Exception {
        // Override setup: Matikan auth context
        when(authContext.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/api/health-records"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("User tidak terautentikasi"));
    }

    @Test
    @DisplayName("Get Record By ID - Sukses")
    void testGetRecordById_Success() throws Exception {
        UUID recordId = mockHealthRecord.getId();

        when(healthRecordService.getHealthRecordById(any(UUID.class), eq(recordId)))
                .thenReturn(mockHealthRecord);

        mockMvc.perform(get("/api/health-records/{id}", recordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Data catatan berhasil diambil"))
                .andExpect(jsonPath("$.data.record").exists());
    }

    @Test
    @DisplayName("Get Record By ID - Not Found")
    void testGetRecordById_NotFound() throws Exception {
        UUID recordId = UUID.randomUUID();

        when(healthRecordService.getHealthRecordById(any(UUID.class), eq(recordId)))
                .thenReturn(null);

        mockMvc.perform(get("/api/health-records/{id}", recordId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Data catatan tidak ditemukan"));
    }

    @Test
    @DisplayName("Get Record By ID - Unauthorized")
    void testGetRecordById_Unauthorized() throws Exception {
        // Override setup: Matikan auth context
        when(authContext.isAuthenticated()).thenReturn(false);

        UUID recordId = UUID.randomUUID();

        mockMvc.perform(get("/api/health-records/{id}", recordId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("User tidak terautentikasi"));
    }

    @Test
    @DisplayName("Update Record - Sukses")
    void testUpdateRecord_Success() throws Exception {
        UUID recordId = mockHealthRecord.getId();

        HealthRecord updateReq = new HealthRecord();
        updateReq.setBodyTemperature(36.8);
        updateReq.setBloodPressure("125/82");
        updateReq.setHeartRate(78);

        when(healthRecordService.updateHealthRecord(any(UUID.class), eq(recordId), any(HealthRecord.class)))
                .thenReturn(mockHealthRecord);

        mockMvc.perform(put("/api/health-records/{id}", recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Data catatan berhasil diperbarui"));
    }

    @Test
    @DisplayName("Update Record - Gagal (Suhu tidak valid)")
    void testUpdateRecord_Fail_InvalidBodyTemperature() throws Exception {
        UUID recordId = mockHealthRecord.getId();

        HealthRecord updateReq = new HealthRecord();
        updateReq.setBloodPressure("125/82");
        updateReq.setHeartRate(78);

        mockMvc.perform(put("/api/health-records/{id}", recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Suhu tubuh tidak valid"));
    }

    @Test
    @DisplayName("Update Record - Not Found")
    void testUpdateRecord_NotFound() throws Exception {
        UUID recordId = UUID.randomUUID();

        HealthRecord updateReq = new HealthRecord();
        updateReq.setBodyTemperature(36.8);
        updateReq.setBloodPressure("125/82");
        updateReq.setHeartRate(78);

        when(healthRecordService.updateHealthRecord(any(UUID.class), eq(recordId), any(HealthRecord.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/health-records/{id}", recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Data catatan tidak ditemukan atau akses ditolak"));
    }

    @Test
    @DisplayName("Update Record - Unauthorized")
    void testUpdateRecord_Unauthorized() throws Exception {
        // Override setup: Matikan auth context
        when(authContext.isAuthenticated()).thenReturn(false);

        UUID recordId = UUID.randomUUID();

        HealthRecord updateReq = new HealthRecord();
        updateReq.setBodyTemperature(36.8);
        updateReq.setBloodPressure("125/82");
        updateReq.setHeartRate(78);

        mockMvc.perform(put("/api/health-records/{id}", recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("User tidak terautentikasi"));
    }

    @Test
    @DisplayName("Delete Record - Sukses")
    void testDeleteRecord_Success() throws Exception {
        UUID recordId = mockHealthRecord.getId();

        when(healthRecordService.deleteHealthRecord(any(UUID.class), eq(recordId)))
                .thenReturn(true);

        mockMvc.perform(delete("/api/health-records/{id}", recordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Data catatan berhasil dihapus"));
    }

    @Test
    @DisplayName("Delete Record - Not Found")
    void testDeleteRecord_NotFound() throws Exception {
        UUID recordId = UUID.randomUUID();

        when(healthRecordService.deleteHealthRecord(any(UUID.class), eq(recordId)))
                .thenReturn(false);

        mockMvc.perform(delete("/api/health-records/{id}", recordId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Data catatan tidak ditemukan"));
    }

    @Test
    @DisplayName("Delete Record - Unauthorized")
    void testDeleteRecord_Unauthorized() throws Exception {
        // Override setup: Matikan auth context
        when(authContext.isAuthenticated()).thenReturn(false);

        UUID recordId = UUID.randomUUID();

        mockMvc.perform(delete("/api/health-records/{id}", recordId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("User tidak terautentikasi"));
    }

    @Test
    @DisplayName("Update Record - Sukses (Semua field diisi)")
    void testUpdateRecord_Success_AllFields() throws Exception {
        UUID recordId = mockHealthRecord.getId();

        HealthRecord updateReq = new HealthRecord();
        updateReq.setBodyTemperature(36.8);
        updateReq.setBloodPressure("125/82");
        updateReq.setHeartRate(78);
        updateReq.setNotes("Update catatan");

        when(healthRecordService.updateHealthRecord(any(UUID.class), eq(recordId), any(HealthRecord.class)))
                .thenReturn(mockHealthRecord);

        mockMvc.perform(put("/api/health-records/{id}", recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Data catatan berhasil diperbarui"));
    }
}