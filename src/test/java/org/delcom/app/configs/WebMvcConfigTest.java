package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebMvcConfigTest {

    @Mock
    private AuthInterceptor authInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @Mock
    private ResourceHandlerRegistry resourceHandlerRegistry;

    @Mock
    private ResourceHandlerRegistration resourceHandlerRegistration;

    @InjectMocks
    private WebMvcConfig webMvcConfig;

    @Test
    @DisplayName("Test Konfigurasi Interceptor (Path & Exclude)")
    void testAddInterceptors() {
        // 1. Arrange (Siapkan Mock Chain)
        // Saat registry.addInterceptor dipanggil, kembalikan object registration (untuk chaining method)
        when(interceptorRegistry.addInterceptor(authInterceptor)).thenReturn(interceptorRegistration);
        
        // Saat addPathPatterns dipanggil, kembalikan object registration lagi (Fluent API)
        when(interceptorRegistration.addPathPatterns(anyString())).thenReturn(interceptorRegistration);

        // 2. Act
        webMvcConfig.addInterceptors(interceptorRegistry);

        // 3. Assert (Verifikasi Konfigurasi)
        
        // Pastikan authInterceptor didaftarkan
        verify(interceptorRegistry).addInterceptor(authInterceptor);

        // Pastikan Path Pattern yang didaftarkan adalah "/api/**"
        verify(interceptorRegistration).addPathPatterns("/api/**");

        // Pastikan Path yang DIKECUALIKAN adalah "/api/auth/**"
        // Kita pakai varArgs matchers
        verify(interceptorRegistration).excludePathPatterns("/api/auth/**");
    }

    @Test
    @DisplayName("Test Konfigurasi Resource Handler (Assets)")
    void testAddResourceHandlers() {
        // 1. Arrange
        // Saat addResourceHandler dipanggil, kembalikan object registration
        when(resourceHandlerRegistry.addResourceHandler(anyString())).thenReturn(resourceHandlerRegistration);

        // 2. Act
        webMvcConfig.addResourceHandlers(resourceHandlerRegistry);

        // 3. Assert
        
        // Pastikan URL "/assets/**" didaftarkan
        verify(resourceHandlerRegistry).addResourceHandler("/assets/**");
        
        // Pastikan lokasi folder fisiknya benar di "classpath:/static/assets/"
        verify(resourceHandlerRegistration).addResourceLocations("classpath:/static/assets/");
    }
}