package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private AuthContext authContext;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    // --- TARGET 1: Line 34 & 92 (Public Endpoints Logic) ---

    @Test
    @DisplayName("Endpoint Public (/api/auth) - Logic: True || ...")
    void testPublicEndpoint_ApiAuth() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login"); // Memicu startWith("/api/auth") = TRUE
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        // Memastikan tidak lanjut ke validasi token
        verifyNoInteractions(authTokenService);
    }

    @Test
    @DisplayName("Endpoint Public (/error) - Logic: False || True")
    void testPublicEndpoint_Error() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/error"); // Memicu equals("/error") = TRUE
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    // --- TARGET 2: Line 81 (Header Extraction Logic) ---

    @Test
    @DisplayName("Header Null - Logic: False && ...")
    void testMissingTokenHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        // Header Authorization TIDAK DI-SET (Null)
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Header Invalid Prefix - Logic: True && False")
    void testHeaderNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz"); // Tidak mulai dengan "Bearer "
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    // --- TARGET 3: Line 43 (Token Validation Logic: null OR Empty) ---

    // Case 1: Token Null sudah tercover oleh testMissingTokenHeader di atas.

    // Case 2: Token Empty (Ini yang sering bikin kuning)
    @Test
    @DisplayName("Token Kosong (Hanya prefix 'Bearer ') - Logic: False || True")
    void testEmptyTokenAfterBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        
        // Header ada "Bearer " tapi belakangnya kosong
        // Saat disubstring(7), hasilnya "" (Empty String)
        request.addHeader("Authorization", "Bearer "); 
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token autentikasi tidak ditemukan");
    }

    // --- TARGET 4: Line 49 & 57 (Jwt Validation) ---

    @Test
    @DisplayName("Token Invalid Signature")
    void testInvalidTokenFormat() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            // validateToken returns FALSE
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), eq(true))).thenReturn(false);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("Token autentikasi tidak valid");
        }
    }

    @Test
    @DisplayName("Token Valid tapi UserID Null")
    void testValidTokenButNullUserId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        request.addHeader("Authorization", "Bearer token-no-userid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), eq(true))).thenReturn(true);
            // extractUserId returns NULL
            mockedJwt.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("Format token autentikasi tidak valid");
        }
    }

    // --- TARGET 5: Database & Success Flow ---

    @Test
    @DisplayName("Token Tidak Ada di DB (Expired/Logout)")
    void testTokenNotFoundInDB() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        request.addHeader("Authorization", "Bearer token-expired");
        MockHttpServletResponse response = new MockHttpServletResponse();
        UUID userId = UUID.randomUUID();

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), eq(true))).thenReturn(true);
            mockedJwt.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(userId);

            // DB return null
            when(authTokenService.findUserToken(eq(userId), anyString())).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("Token autentikasi sudah expired");
        }
    }

    @Test
    @DisplayName("User Tidak Ditemukan di DB")
    void testUserNotFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        request.addHeader("Authorization", "Bearer token-valid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        UUID userId = UUID.randomUUID();
        AuthToken authToken = new AuthToken(userId, "token-valid");

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), eq(true))).thenReturn(true);
            mockedJwt.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(userId);

            when(authTokenService.findUserToken(eq(userId), anyString())).thenReturn(authToken);
            // UserService return null
            when(userService.getUserById(userId)).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(response.getContentAsString()).contains("User tidak ditemukan");
        }
    }

    @Test
    @DisplayName("Happy Path - Sukses")
    void testSuccess() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        request.addHeader("Authorization", "Bearer token-sukses");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        UUID userId = UUID.randomUUID();
        AuthToken authToken = new AuthToken(userId, "token-sukses");
        User user = new User();
        user.setId(userId);

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), eq(true))).thenReturn(true);
            mockedJwt.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(userId);

            when(authTokenService.findUserToken(eq(userId), anyString())).thenReturn(authToken);
            when(userService.getUserById(userId)).thenReturn(user);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isTrue();
            assertThat(response.getStatus()).isEqualTo(200);
            verify(authContext).setAuthUser(user);
        }
    }
}