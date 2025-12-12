package org.delcom.app.views;

import jakarta.servlet.http.HttpSession;
import org.delcom.app.dto.HealthRecordForm;
import org.delcom.app.dto.HealthRecordPhotoForm;
import org.delcom.app.entities.HealthRecord;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.HealthRecordService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction; // PENTING
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource; // PENTING
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthRecordViewTest {

    @Mock private HealthRecordService healthRecordService;
    @Mock private FileStorageService fileStorageService;
    @Mock private RedirectAttributes redirectAttributes;
    @Mock private HttpSession session;
    @Mock private Model model;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private HealthRecordView healthRecordView;

    private MockedStatic<SecurityContextHolder> securityMock;
    private User mockUser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        securityMock = mockStatic(SecurityContextHolder.class);
        securityMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        securityMock.close();
    }

    private void mockLogin() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
    }

    private void mockAnonymous() {
        Authentication anonToken = mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(anonToken);
    }

    // ==========================================
    // 1. POST ADD RECORD
    // ==========================================

    @Test
    @DisplayName("Add Record - Service Returns Null (Fail)")
    void testPostAddRecord_ServiceFail() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm();
        form.setBodyTemperature(36.5);

        when(healthRecordService.createHealthRecord(any(), any())).thenReturn(null);

        String view = healthRecordView.postAddRecord(form, redirectAttributes, session, model);
        
        assertEquals("redirect:/", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Gagal menyimpan"));
    }

    @Test
    @DisplayName("Add Record - Not Login")
    void testPostAddRecord_NotLogin() {
        mockAnonymous();
        String view = healthRecordView.postAddRecord(new HealthRecordForm(), redirectAttributes, session, model);
        assertEquals("redirect:/auth/logout", view);
    }

    @Test
    @DisplayName("Add Record - Validation Fail")
    void testPostAddRecord_ValidationFail() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm(); // Suhu null
        String view = healthRecordView.postAddRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
    }

    @Test
    @DisplayName("Add Record - Success")
    void testPostAddRecord_Success() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm();
        form.setBodyTemperature(36.5);
        when(healthRecordService.createHealthRecord(any(), any())).thenReturn(new HealthRecord());
        String view = healthRecordView.postAddRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
    }

    // ==========================================
    // 2. POST EDIT RECORD
    // ==========================================

    @Test
    @DisplayName("Edit Record - Update Fail")
    void testPostEditRecord_UpdateFail() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm();
        form.setId(UUID.randomUUID());
        
        when(healthRecordService.updateHealthRecord(any(), any(), any())).thenReturn(null);

        String view = healthRecordView.postEditRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Gagal memperbarui"));
    }

    @Test
    @DisplayName("Edit Record - Not Login")
    void testPostEditRecord_NotLogin() {
        mockAnonymous();
        String view = healthRecordView.postEditRecord(new HealthRecordForm(), redirectAttributes, session, model);
        assertEquals("redirect:/auth/logout", view);
    }

    @Test
    @DisplayName("Edit Record - ID Null")
    void testPostEditRecord_IdNull() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm(); // ID null
        String view = healthRecordView.postEditRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("ID catatan tidak valid"));
    }

    @Test
    @DisplayName("Edit Record - Success")
    void testPostEditRecord_Success() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm();
        form.setId(UUID.randomUUID());
        when(healthRecordService.updateHealthRecord(any(), any(), any())).thenReturn(new HealthRecord());
        String view = healthRecordView.postEditRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
    }

    // ==========================================
    // 3. DELETE & DETAIL
    // ==========================================

    @Test
    @DisplayName("Get Detail - Not Found")
    void testGetDetail_NotFound() {
        mockLogin();
        when(healthRecordService.getHealthRecordById(any(), any())).thenReturn(null);
        String view = healthRecordView.getDetailRecord(UUID.randomUUID(), model);
        assertEquals("redirect:/", view); 
    }

    @Test
    @DisplayName("Get Detail - Not Login")
    void testGetDetail_NotLogin() {
        mockAnonymous();
        String view = healthRecordView.getDetailRecord(UUID.randomUUID(), model);
        assertEquals("redirect:/auth/logout", view);
    }

    @Test
    @DisplayName("Get Detail - Success")
    void testGetDetail_Success() {
        mockLogin();
        when(healthRecordService.getHealthRecordById(any(), any())).thenReturn(new HealthRecord());
        String view = healthRecordView.getDetailRecord(UUID.randomUUID(), model);
        assertEquals(ConstUtil.TEMPLATE_PAGES_HEALTH_DETAIL, view);
    }

    @Test
    @DisplayName("Delete Record - Fail")
    void testPostDeleteRecord_Fail() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm();
        form.setId(UUID.randomUUID());
        
        when(healthRecordService.deleteHealthRecord(any(), any())).thenReturn(false);
        
        String view = healthRecordView.postDeleteRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Gagal menghapus"));
    }

    @Test
    @DisplayName("Delete Record - Not Login")
    void testPostDeleteRecord_NotLogin() {
        mockAnonymous();
        HealthRecordForm form = new HealthRecordForm();
        String view = healthRecordView.postDeleteRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/auth/logout", view);
    }

    @Test
    @DisplayName("Delete Record - ID Null")
    void testPostDeleteRecord_IdNull() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm();
        form.setId(null); // Null ID
        String view = healthRecordView.postDeleteRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("ID tidak valid"));
    }

    @Test
    @DisplayName("Delete Record - Success")
    void testPostDeleteRecord_Success() {
        mockLogin();
        HealthRecordForm form = new HealthRecordForm();
        form.setId(UUID.randomUUID());
        when(healthRecordService.deleteHealthRecord(any(), any())).thenReturn(true);
        String view = healthRecordView.postDeleteRecord(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
    }

    // ==========================================
    // 4. UPLOAD PHOTO
    // ==========================================

    @Test
    @DisplayName("Upload Photo - Not Login")
    void testUploadPhoto_NotLogin() {
        mockAnonymous();
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();
        String view = healthRecordView.postEditPhoto(form, redirectAttributes, session, model);
        assertEquals("redirect:/auth/logout", view);
    }

    @Test
    @DisplayName("Upload Photo - Record Not Found")
    void testUploadPhoto_RecordNotFound() {
        mockLogin();
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();
        form.setId(UUID.randomUUID());
        form.setPhotoFile(new MockMultipartFile("file", "img.jpg", "image/jpeg", "content".getBytes()));

        when(healthRecordService.getHealthRecordById(any(), any())).thenReturn(null);

        String view = healthRecordView.postEditPhoto(form, redirectAttributes, session, model);
        assertEquals("redirect:/", view);
    }

    @Test
    @DisplayName("Upload Photo - Size Invalid")
    void testUploadPhoto_SizeInvalid() {
        mockLogin();
        HealthRecordPhotoForm mockForm = mock(HealthRecordPhotoForm.class);
        
        when(mockForm.getId()).thenReturn(UUID.randomUUID());
        when(mockForm.isEmpty()).thenReturn(false);
        when(mockForm.isValidImage()).thenReturn(true);
        when(healthRecordService.getHealthRecordById(any(), any())).thenReturn(new HealthRecord());

        when(mockForm.isSizeValid(anyLong())).thenReturn(false);

        String view = healthRecordView.postEditPhoto(mockForm, redirectAttributes, session, model);
        assertEquals("redirect:/health-records/" + mockForm.getId(), view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("terlalu besar"));
    }

    @Test
    @DisplayName("Upload Photo - IOException")
    void testUploadPhoto_IOException() throws IOException {
        mockLogin();
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();
        form.setId(UUID.randomUUID());
        form.setPhotoFile(new MockMultipartFile("file", "img.jpg", "image/jpeg", "c".getBytes()));

        when(healthRecordService.getHealthRecordById(any(), any())).thenReturn(new HealthRecord());
        when(fileStorageService.storeFile(any(), any())).thenThrow(new IOException("Disk Full"));

        String view = healthRecordView.postEditPhoto(form, redirectAttributes, session, model);
        assertEquals("redirect:/health-records/" + form.getId(), view);
    }

    @Test
    @DisplayName("Upload Photo - Empty File")
    void testUploadPhoto_Empty() {
        mockLogin();
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();
        form.setId(UUID.randomUUID());
        form.setPhotoFile(new MockMultipartFile("file", "", "image/png", new byte[0]));
        
        String view = healthRecordView.postEditPhoto(form, redirectAttributes, session, model);
        assertEquals("redirect:/health-records/" + form.getId(), view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("tidak boleh kosong"));
    }

    @Test
    @DisplayName("Upload Photo - Invalid Type")
    void testUploadPhoto_InvalidType() {
        mockLogin();
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();
        form.setId(UUID.randomUUID());
        form.setPhotoFile(new MockMultipartFile("file", "a.pdf", "application/pdf", "c".getBytes()));
        
        when(healthRecordService.getHealthRecordById(any(), any())).thenReturn(new HealthRecord());
        
        String view = healthRecordView.postEditPhoto(form, redirectAttributes, session, model);
        assertEquals("redirect:/health-records/" + form.getId(), view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Format file harus"));
    }

    @Test
    @DisplayName("Upload Photo - Success")
    void testUploadPhoto_Success() throws IOException {
        mockLogin();
        HealthRecordPhotoForm form = new HealthRecordPhotoForm();
        form.setId(UUID.randomUUID());
        form.setPhotoFile(new MockMultipartFile("file", "a.jpg", "image/jpeg", "c".getBytes()));
        
        when(healthRecordService.getHealthRecordById(any(), any())).thenReturn(new HealthRecord());
        when(fileStorageService.storeFile(any(), any())).thenReturn("ok.jpg");
        
        String view = healthRecordView.postEditPhoto(form, redirectAttributes, session, model);
        assertEquals("redirect:/health-records/" + form.getId(), view);
    }

    // ==========================================
    // 5. GET PHOTO & AUTH USER (Termasuk Fix Branch Kuning)
    // ==========================================

    @Test
    @DisplayName("Get Photo - Success (Exists True)")
    void testGetPhoto_Success() throws IOException {
        Path dummyFile = tempDir.resolve("avatar.jpg");
        Files.createFile(dummyFile);
        when(fileStorageService.loadFile("avatar.jpg")).thenReturn(dummyFile);
        Resource res = healthRecordView.getPhotoByFilename("avatar.jpg");
        assertNotNull(res);
        assertTrue(res.exists());
    }

    @Test
    @DisplayName("Get Photo - Exception")
    void testGetPhoto_Exception() {
        when(fileStorageService.loadFile(anyString())).thenThrow(new RuntimeException("Error"));
        Resource res = healthRecordView.getPhotoByFilename("error.jpg");
        assertNull(res);
    }

    @Test
    @DisplayName("Get Photo - File Not Found")
    void testGetPhoto_NotFound() {
        Path path = Paths.get("non-existent.jpg");
        when(fileStorageService.loadFile("non-existent.jpg")).thenReturn(path);
        assertNull(healthRecordView.getPhotoByFilename("non-existent.jpg"));
    }

    // === SOLUSI UNTUK KUNING DI BARIS 245 ===
    // Kita memalsukan UrlResource agar exists()=FALSE tapi isReadable()=TRUE
    @Test
    @DisplayName("Get Photo - Branch Coverage (Exists=False, Readable=True)")
    void testGetPhoto_BranchCoverage() {
        // 1. Stub loadFile
        Path dummyPath = Paths.get("dummy.jpg");
        when(fileStorageService.loadFile("dummy.jpg")).thenReturn(dummyPath);

        // 2. Mock Constructor UrlResource untuk memanipulasi return value methodnya
        // Try-with-resources untuk mockConstruction
        try (MockedConstruction<UrlResource> mocked = mockConstruction(UrlResource.class,
                (mock, context) -> {
                    // Logic ini menembak kondisi: IF (false OR true) -> Masuk
                    doReturn(false).when(mock).exists();
                    doReturn(true).when(mock).isReadable();
                })) {

            // 3. Act
            Resource res = healthRecordView.getPhotoByFilename("dummy.jpg");

            // 4. Assert
            assertNotNull(res); // Masuk ke if (return resource) karena OR operator
            assertEquals(1, mocked.constructed().size());
        }
    }

    @Test
    @DisplayName("Get Auth User - Helper Check")
    void testGetAuthUser_Helper() {
        Authentication anonToken = mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(anonToken);
        assertNull(callGetAuthUser());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("NotUserEntity");
        assertNull(callGetAuthUser());

        when(authentication.getPrincipal()).thenReturn(mockUser);
        assertNotNull(callGetAuthUser());
    }

    private User callGetAuthUser() {
        try {
            java.lang.reflect.Method method = HealthRecordView.class.getDeclaredMethod("getAuthUser");
            method.setAccessible(true);
            return (User) method.invoke(healthRecordView);
        } catch (Exception e) {
            return null;
        }
    }
}