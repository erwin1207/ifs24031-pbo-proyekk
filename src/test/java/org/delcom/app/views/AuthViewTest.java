package org.delcom.app.views;

import jakarta.servlet.http.HttpSession;
import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthViewTest {

    @Mock private UserService userService;
    @Mock private AuthTokenService authTokenService;
    @Mock private Model model;
    @Mock private HttpSession session;
    @Mock private BindingResult bindingResult;
    @Mock private RedirectAttributes redirectAttributes;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthView authView;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUp() {
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    // ==========================================
    // 1. SHOW LOGIN (GET) - BRANCH COVERAGE FIX
    // ==========================================

    @Test
    @DisplayName("Show Login - Auth Null (Fix Branch)")
    void testShowLogin_AuthNull() {
        // Kondisi 1: auth == null
        when(securityContext.getAuthentication()).thenReturn(null);

        String view = authView.showLogin(model, session);

        // isLoggedIn = false -> Tampil halaman login
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    @DisplayName("Show Login - Not Authenticated (Fix Branch)")
    void testShowLogin_NotAuthenticated() {
        // Kondisi 2: auth != null TAPI isAuthenticated() == false
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        String view = authView.showLogin(model, session);

        // isLoggedIn = false -> Tampil halaman login
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    @DisplayName("Show Login - Anonymous User")
    void testShowLogin_Anonymous() {
        // Kondisi 3: auth != null, auth == true, TAPI instance of Anonymous
        AnonymousAuthenticationToken anonToken = mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(anonToken);
        when(anonToken.isAuthenticated()).thenReturn(true);

        String view = authView.showLogin(model, session);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    @DisplayName("Show Login - Already Logged In")
    void testShowLogin_AlreadyLoggedIn() {
        // Kondisi 4: User beneran (Login Sukses)
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        // authentication BUKAN instance of Anonymous

        String view = authView.showLogin(model, session);

        assertEquals("redirect:/", view);
    }

    // ==========================================
    // 2. SHOW REGISTER (GET) - BRANCH COVERAGE FIX
    // ==========================================

    @Test
    @DisplayName("Show Register - Auth Null (Fix Branch)")
    void testShowRegister_AuthNull() {
        when(securityContext.getAuthentication()).thenReturn(null);
        String view = authView.showRegister(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    @DisplayName("Show Register - Not Authenticated (Fix Branch)")
    void testShowRegister_NotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        String view = authView.showRegister(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    @DisplayName("Show Register - Anonymous")
    void testShowRegister_Anonymous() {
        AnonymousAuthenticationToken anonToken = mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(anonToken);
        when(anonToken.isAuthenticated()).thenReturn(true);

        String view = authView.showRegister(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    @DisplayName("Show Register - Already Logged In")
    void testShowRegister_AlreadyLoggedIn() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        String view = authView.showRegister(model, session);
        assertEquals("redirect:/", view);
    }

    // ==========================================
    // 3. POST LOGIN
    // ==========================================

    @Test
    @DisplayName("Post Login - Validasi Form Gagal")
    void testPostLogin_ValidationErrors() {
        LoginForm form = new LoginForm();
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = authView.postLogin(form, bindingResult, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Post Login - User Tidak Ditemukan")
    void testPostLogin_UserNotFound() {
        LoginForm form = new LoginForm();
        form.setEmail("ghost@delcom.org");
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail(form.getEmail())).thenReturn(null);

        String view = authView.postLogin(form, bindingResult, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), anyString());
    }

    @Test
    @DisplayName("Post Login - Password Salah")
    void testPostLogin_WrongPassword() {
        LoginForm form = new LoginForm();
        form.setEmail("test@delcom.org");
        form.setPassword("wrongpass");

        User dbUser = new User();
        dbUser.setPassword(new BCryptPasswordEncoder().encode("realpass"));
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail(form.getEmail())).thenReturn(dbUser);

        String view = authView.postLogin(form, bindingResult, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), anyString());
    }

    @Test
    @DisplayName("Post Login - Sukses")
    void testPostLogin_Success() {
        LoginForm form = new LoginForm();
        form.setEmail("test@delcom.org");
        form.setPassword("realpass");

        User dbUser = new User();
        dbUser.setPassword(new BCryptPasswordEncoder().encode("realpass"));

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail(form.getEmail())).thenReturn(dbUser);

        SecurityContext newContext = mock(SecurityContext.class);
        securityContextHolderMock.when(SecurityContextHolder::createEmptyContext).thenReturn(newContext);

        String view = authView.postLogin(form, bindingResult, session, model);

        assertEquals("redirect:/", view);
        
        securityContextHolderMock.verify(SecurityContextHolder::createEmptyContext);
        verify(session).setAttribute(anyString(), eq(newContext));
    }

    // ==========================================
    // 4. POST REGISTER
    // ==========================================

    @Test
    @DisplayName("Post Register - Validasi Gagal")
    void testPostRegister_ValidationErrors() {
        RegisterForm form = new RegisterForm();
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = authView.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    @DisplayName("Post Register - Email Sudah Ada")
    void testPostRegister_EmailExists() {
        RegisterForm form = new RegisterForm();
        form.setEmail("exist@delcom.org");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail(form.getEmail())).thenReturn(new User());

        String view = authView.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), anyString());
    }

    @Test
    @DisplayName("Post Register - Gagal Create User")
    void testPostRegister_CreateFailed() {
        RegisterForm form = new RegisterForm();
        form.setEmail("new@delcom.org");
        form.setPassword("pass");
        form.setName("Name");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail(form.getEmail())).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(null);

        String view = authView.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), anyString());
    }

    @Test
    @DisplayName("Post Register - Sukses")
    void testPostRegister_Success() {
        RegisterForm form = new RegisterForm();
        form.setEmail("new@delcom.org");
        form.setPassword("pass");
        form.setName("Name");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail(form.getEmail())).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(new User());

        String view = authView.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals("redirect:/auth/login", view);
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    // ==========================================
    // 5. LOGOUT
    // ==========================================

    @Test
    @DisplayName("Logout - Sukses")
    void testLogout() {
        String view = authView.logout(session);

        assertEquals("redirect:/auth/login", view);
        verify(session).invalidate();
    }
}