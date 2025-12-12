package org.delcom.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.interceptors.AuthInterceptor;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class,
    OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenService authTokenService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @Autowired
    private AuthContext authContext;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        // === PERBAIKAN UTAMA: RESET MOCK MANUAL ===
        // Karena authContext adalah @Bean manual, history panggilannya 
        // tersimpan antar test. Kita wajib reset agar verify() akurat.
        reset(authContext); 
        // ==========================================

        // 1. Setup User Dummy
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("Tester");
        mockUser.setEmail("test@delcom.org");
        mockUser.setPassword(passwordEncoder.encode("password123"));

        // 2. Setup Default Auth: Login Sukses
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);

        // 3. Bypass Interceptor
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    // ========================================================================
    // 1. GET USER INFO
    // ========================================================================

    @Test
    @DisplayName("Get Info - Unauthorized (Cover Branch 1)")
    void testGetUserInfo_Unauthorized() throws Exception {
        // Force logout
        when(authContext.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get Info - Success (Cover Instructions Red Lines)")
    void testGetUserInfo_Success() throws Exception {
        // Pastikan login
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.email").value("test@delcom.org"));
        
        // Verifikasi bahwa getAuthUser dipanggil 1 kali DI TEST INI SAJA
        // (Berhasil karena kita sudah reset() di setUp)
        verify(authContext, times(1)).getAuthUser();
    }

    // ========================================================================
    // 2. REGISTER USER
    // ========================================================================

    @Test
    @DisplayName("Register - Name NULL")
    void testRegister_NameNull() throws Exception {
        User u = new User(null, "a@b.c", "pass");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Name EMPTY")
    void testRegister_NameEmpty() throws Exception {
        User u = new User("", "a@b.c", "pass");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Email NULL")
    void testRegister_EmailNull() throws Exception {
        User u = new User("Name", null, "pass");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Email EMPTY")
    void testRegister_EmailEmpty() throws Exception {
        User u = new User("Name", "", "pass");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Password NULL")
    void testRegister_PassNull() throws Exception {
        User u = new User("Name", "a@b.c", null);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Password EMPTY")
    void testRegister_PassEmpty() throws Exception {
        User u = new User("Name", "a@b.c", "");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - User Exists")
    void testRegister_UserExists() throws Exception {
        User u = new User("Budi", "exist@delcom.org", "123");
        when(userService.getUserByEmail(u.getEmail())).thenReturn(new User());
        
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pengguna sudah terdaftar dengan email ini"));
    }

    @Test
    @DisplayName("Register - Success")
    void testRegister_Success() throws Exception {
        User u = new User("Budi", "new@delcom.org", "123");
        when(userService.getUserByEmail(u.getEmail())).thenReturn(null);
        when(userService.createUser(any(), any(), any())).thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isOk());
    }

    // ========================================================================
    // 3. LOGIN USER
    // ========================================================================

    @Test
    @DisplayName("Login - Email NULL")
    void testLogin_EmailNull() throws Exception {
        User u = new User(null, "pass");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login - Email EMPTY")
    void testLogin_EmailEmpty() throws Exception {
        User u = new User("", "pass");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login - Password NULL")
    void testLogin_PassNull() throws Exception {
        User u = new User("a@b.c", null);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login - Password EMPTY")
    void testLogin_PassEmpty() throws Exception {
        User u = new User("a@b.c", "");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login - User Not Found")
    void testLogin_UserNotFound() throws Exception {
        User u = new User("ghost@b.c", "123");
        when(userService.getUserByEmail(u.getEmail())).thenReturn(null);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login - Wrong Password")
    void testLogin_WrongPass() throws Exception {
        User u = new User("test@delcom.org", "wrong");
        when(userService.getUserByEmail(u.getEmail())).thenReturn(mockUser);
        
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email atau password salah"));
    }

    @Test
    @DisplayName("Login - Token Creation Failed (500)")
    void testLogin_TokenFail() throws Exception {
        User u = new User("test@delcom.org", "password123");
        when(userService.getUserByEmail(u.getEmail())).thenReturn(mockUser);
        when(authTokenService.createAuthToken(any())).thenReturn(null); // Return Null

        try (MockedStatic<JwtUtil> jwt = mockStatic(JwtUtil.class)) {
            jwt.when(() -> JwtUtil.generateToken(any())).thenReturn("token");
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(u)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Login - Success & Cleanup Token")
    void testLogin_Success() throws Exception {
        User u = new User("test@delcom.org", "password123");
        when(userService.getUserByEmail(u.getEmail())).thenReturn(mockUser);
        
        AuthToken oldToken = new AuthToken();
        when(authTokenService.findUserToken(any(), any())).thenReturn(oldToken); // Ada token lama
        when(authTokenService.createAuthToken(any())).thenReturn(new AuthToken());

        try (MockedStatic<JwtUtil> jwt = mockStatic(JwtUtil.class)) {
            jwt.when(() -> JwtUtil.generateToken(any())).thenReturn("token");
            
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(u)))
                    .andExpect(status().isOk());
            
            verify(authTokenService).deleteAuthToken(mockUser.getId());
        }
    }

    // ========================================================================
    // 4. UPDATE USER
    // ========================================================================

    @Test
    @DisplayName("Update User - Unauthorized")
    void testUpdateUser_Unauth() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(false);
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Update User - Name NULL")
    void testUpdateUser_NameNull() throws Exception {
        User u = new User(null, "a@b.c", null);
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update User - Name EMPTY")
    void testUpdateUser_NameEmpty() throws Exception {
        User u = new User("", "a@b.c", null);
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update User - Email NULL")
    void testUpdateUser_EmailNull() throws Exception {
        User u = new User("Name", null, null);
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update User - Email EMPTY")
    void testUpdateUser_EmailEmpty() throws Exception {
        User u = new User("Name", "", null);
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update User - Service Returns Null (404)")
    void testUpdateUser_NotFound() throws Exception {
        User u = new User("Name", "a@b.c", null);
        when(userService.updateUser(any(), any(), any())).thenReturn(null);
        
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update User - Success")
    void testUpdateUser_Success() throws Exception {
        User u = new User("Name", "a@b.c", null);
        when(userService.updateUser(any(), any(), any())).thenReturn(mockUser);
        
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(u))).andExpect(status().isOk());
    }

    // ========================================================================
    // 5. UPDATE PASSWORD
    // ========================================================================

    @Test
    @DisplayName("Update Pass - Unauthorized")
    void testUpdatePass_Unauth() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(false);
        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Update Pass - OldPass NULL")
    void testUpdatePass_OldNull() throws Exception {
        Map<String, String> p = new HashMap<>(); p.put("password", null); p.put("newPassword", "n");
        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update Pass - OldPass EMPTY")
    void testUpdatePass_OldEmpty() throws Exception {
        Map<String, String> p = new HashMap<>(); p.put("password", ""); p.put("newPassword", "n");
        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update Pass - NewPass NULL")
    void testUpdatePass_NewNull() throws Exception {
        Map<String, String> p = new HashMap<>(); p.put("password", "o"); p.put("newPassword", null);
        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update Pass - NewPass EMPTY")
    void testUpdatePass_NewEmpty() throws Exception {
        Map<String, String> p = new HashMap<>(); p.put("password", "o"); p.put("newPassword", "");
        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update Pass - Wrong Old Pass")
    void testUpdatePass_Wrong() throws Exception {
        Map<String, String> p = new HashMap<>(); 
        p.put("password", "wrong"); 
        p.put("newPassword", "new");
        
        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Konfirmasi password tidak cocok"));
    }

    @Test
    @DisplayName("Update Pass - Service Returns Null (404)")
    void testUpdatePass_NotFound() throws Exception {
        Map<String, String> p = new HashMap<>(); 
        p.put("password", "password123"); 
        p.put("newPassword", "new");
        
        when(userService.updatePassword(any(), any())).thenReturn(null);

        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update Pass - Success")
    void testUpdatePass_Success() throws Exception {
        Map<String, String> p = new HashMap<>(); 
        p.put("password", "password123"); 
        p.put("newPassword", "new");
        
        when(userService.updatePassword(any(), any())).thenReturn(mockUser);

        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p))).andExpect(status().isOk());
        
        verify(authTokenService).deleteAuthToken(mockUser.getId());
    }
}