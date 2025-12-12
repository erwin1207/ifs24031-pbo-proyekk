package org.delcom.app.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @InjectMocks
    private RequestLoggingFilter requestLoggingFilter;

    @Mock
    private FilterChain filterChain;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Test Log 200 OK (Warna Hijau)")
    void testLog200OK() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        requestLoggingFilter.doFilter(request, response, filterChain);

        String output = outputStreamCaptor.toString();
        assertThat(output).contains("\u001B[32m"); // Hijau
        assertThat(output).contains("200");
    }

    @Test
    @DisplayName("Test Log 404 Not Found (Warna Kuning)")
    void testLog404NotFound() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/unknown");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            MockHttpServletResponse res = invocation.getArgument(1);
            res.setStatus(404);
            return null;
        }).when(filterChain).doFilter(any(), any());

        requestLoggingFilter.doFilter(request, response, filterChain);

        String output = outputStreamCaptor.toString();
        assertThat(output).contains("\u001B[33m"); // Kuning
        assertThat(output).contains("404");
    }

    @Test
    @DisplayName("Test Log 500 Error (Warna Merah)")
    void testLog500ServerError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/delete");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            MockHttpServletResponse res = invocation.getArgument(1);
            res.setStatus(500);
            return null;
        }).when(filterChain).doFilter(any(), any());

        requestLoggingFilter.doFilter(request, response, filterChain);

        String output = outputStreamCaptor.toString();
        assertThat(output).contains("\u001B[31m"); // Merah
        assertThat(output).contains("500");
    }

    // --- TAMBAHAN UNTUK MENGHIJAUKAN BARIS 48 (CYAN) ---
    @Test
    @DisplayName("Test Log 100 Continue (Warna Cyan)")
    void testLog100Continue() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/loading");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Simulasi status code 100 (Processing/Continue)
        doAnswer(invocation -> {
            MockHttpServletResponse res = invocation.getArgument(1);
            res.setStatus(100); 
            return null;
        }).when(filterChain).doFilter(any(), any());

        requestLoggingFilter.doFilter(request, response, filterChain);

        String output = outputStreamCaptor.toString();
        // Cek kode warna CYAN (36m)
        assertThat(output).contains("\u001B[36m"); 
        assertThat(output).contains("100");
    }

    @Test
    @DisplayName("Test Ignore Path /.well-known (Tidak boleh ada log)")
    void testIgnoreWellKnown() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/.well-known/pki-validation");
        MockHttpServletResponse response = new MockHttpServletResponse();

        requestLoggingFilter.doFilter(request, response, filterChain);

        String output = outputStreamCaptor.toString();
        assertThat(output).isEmpty();
    }
}