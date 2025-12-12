package org.delcom.app.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName; // Tambah Import
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StartupInfoLoggerTest {

    @InjectMocks
    private StartupInfoLogger startupInfoLogger;

    @Mock
    private ApplicationReadyEvent event;

    @Mock
    private ConfigurableApplicationContext context;

    @Mock
    private ConfigurableEnvironment env;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        // Setup dasar dengan lenient
        lenient().when(event.getApplicationContext()).thenReturn(context);
        lenient().when(context.getEnvironment()).thenReturn(env);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Test Default Startup (Port 8080)")
    void testDefaultStartup() {
        // Gunakan anyString() agar compiler Java 25 tidak bingung method mana yang dipanggil
        lenient().when(env.getProperty(eq("server.port"), anyString())).thenReturn("8080");
        lenient().when(env.getProperty(eq("server.servlet.context-path"), anyString())).thenReturn("/");
        lenient().when(env.getProperty(eq("server.address"), anyString())).thenReturn("localhost");
        
        lenient().when(env.getProperty(eq("spring.devtools.livereload.enabled"), eq(Boolean.class), eq(false)))
                .thenReturn(false);
        lenient().when(env.getProperty(eq("spring.devtools.livereload.port"), anyString())).thenReturn("35729");

        // Act
        startupInfoLogger.onApplicationEvent(event);
        String output = outputStreamCaptor.toString();
        
        // Assert
        assertThat(output).contains("http://localhost:8080");
        assertThat(output).contains("LiveReload: DISABLED");
    }

    // --- TAMBAHAN 1: Test Custom Port & Context Path ---
    @Test
    @DisplayName("Test Custom Startup (Port 9090 & Context /api)")
    void testCustomStartup() {
        // Mocking nilai custom
        lenient().when(env.getProperty(eq("server.port"), anyString())).thenReturn("9090");
        lenient().when(env.getProperty(eq("server.servlet.context-path"), anyString())).thenReturn("/api");
        lenient().when(env.getProperty(eq("server.address"), anyString())).thenReturn("127.0.0.1");

        // Mocking LiveReload Enabled
        lenient().when(env.getProperty(eq("spring.devtools.livereload.enabled"), eq(Boolean.class), eq(false)))
                .thenReturn(true);
        lenient().when(env.getProperty(eq("spring.devtools.livereload.port"), anyString())).thenReturn("1234");

        // Act
        startupInfoLogger.onApplicationEvent(event);
        String output = outputStreamCaptor.toString();

        // Assert
        assertThat(output).contains("http://127.0.0.1:9090/api");
        assertThat(output).contains("LiveReload: ENABLED (port 1234)");
    }

    // --- TAMBAHAN 2: Test Context Path Null ---
    @Test
    @DisplayName("Test Null Context Path (Handling Null Safety)")
    void testNullContextPath() {
        lenient().when(env.getProperty(eq("server.port"), anyString())).thenReturn("8080");
        
        // Simulasi jika property context-path mengembalikan null
        lenient().when(env.getProperty(eq("server.servlet.context-path"), anyString())).thenReturn(null);
        lenient().when(env.getProperty(eq("server.address"), anyString())).thenReturn("localhost");
        
        lenient().when(env.getProperty(eq("spring.devtools.livereload.enabled"), eq(Boolean.class), eq(false)))
                .thenReturn(false);

        // Act
        startupInfoLogger.onApplicationEvent(event);
        String output = outputStreamCaptor.toString();
        
        // Assert: URL harus tetap bersih (tidak ada kata "null")
        assertThat(output).contains("http://localhost:8080");
        assertThat(output).doesNotContain("null");
    }
}