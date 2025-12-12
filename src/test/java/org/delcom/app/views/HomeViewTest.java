package org.delcom.app.views;

import org.delcom.app.entities.HealthRecord;
import org.delcom.app.entities.User;
import org.delcom.app.services.HealthRecordService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeViewTest {

    @Mock
    private HealthRecordService healthRecordService;

    @Mock
    private Model model;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HomeView homeView;

    @BeforeEach
    void setUp() {
        // Set SecurityContextHolder agar menggunakan mock kita
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        // Bersihkan context setelah test
        SecurityContextHolder.clearContext();
    }

    // --- 1. Test User Belum Login (Anonymous) ---
    // Mengcover Baris 28 (True Branch) -> Redirect Logout
    @Test
    @DisplayName("Home - User Anonymous (Belum Login)")
    void testHome_AnonymousUser() {
        // Mocking Authentication sebagai Anonymous
        AnonymousAuthenticationToken anonymousToken = mock(AnonymousAuthenticationToken.class);
        
        when(securityContext.getAuthentication()).thenReturn(anonymousToken);

        String viewName = homeView.home(model);

        assertEquals("redirect:/auth/logout", viewName);
    }

    // --- 2. Test User Login Tapi Principal Salah Tipe ---
    // Mengcover Baris 33 (False Branch Principal check)
    @Test
    @DisplayName("Home - Principal Bukan User Entity")
    void testHome_InvalidPrincipal() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // Principal bukan class User (misal String "admin")
        when(authentication.getPrincipal()).thenReturn("invalid-principal");

        String viewName = homeView.home(model);

        assertEquals("redirect:/auth/logout", viewName);
    }

    // --- 3. Test Happy Path (User Login & Tampil Data) ---
    // Mengcover Baris 38-49 (Semua Instruksi Merah)
    @Test
    @DisplayName("Home - Success (Tampil Halaman)")
    void testHome_Success() {
        // 1. Setup User Dummy
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@delcom.org");

        // 2. Mock Security Context (Authenticated)
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        // 3. Mock Service (Get Data)
        List<HealthRecord> dummyRecords = Collections.emptyList();
        when(healthRecordService.getAllHealthRecords(eq(user.getId()), eq("")))
                .thenReturn(dummyRecords);

        // 4. Execute
        String viewName = homeView.home(model);

        // 5. Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, viewName); // "pages/home"

        // Verifikasi data dimasukkan ke Model
        verify(model).addAttribute("auth", user);
        verify(model).addAttribute("records", dummyRecords);
        verify(model).addAttribute(eq("recordForm"), any());
    }
}