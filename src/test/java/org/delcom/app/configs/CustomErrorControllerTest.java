package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
// PERBAIKAN IMPORT
import org.springframework.boot.web.servlet.error.ErrorAttributes; 

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import jakarta.servlet.http.HttpServletRequest; // Tambah ini

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock; // Tambah mock

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTest {

    @Mock
    private ErrorAttributes errorAttributes;

    // Kita mock HttpServletRequest asli, bukan ServletWebRequest langsung
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CustomErrorController controller;

    @Test
    @DisplayName("Test Error 404 (Not Found)")
    void testHandleError404() {
        // 1. Arrange
        Map<String, Object> mockAttributes = new HashMap<>();
        mockAttributes.put("status", 404);
        mockAttributes.put("path", "/api/hantu");
        mockAttributes.put("error", "Not Found");

        // Perbaikan Mocking: Controller membungkus request menjadi ServletWebRequest di dalam methodnya
        when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(mockAttributes);

        // 2. Act
        // Kita kirim request biasa (HttpServletRequest)
        ResponseEntity<Map<String, Object>> response = controller.handleError(request);

        // 3. Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("status")).isEqualTo("fail");
    }

    @Test
    @DisplayName("Test Error 500")
    void testHandleError500() {
        Map<String, Object> mockAttributes = new HashMap<>();
        mockAttributes.put("status", 500);
        mockAttributes.put("error", "Internal Server Error");

        when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(mockAttributes);

        ResponseEntity<Map<String, Object>> response = controller.handleError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("status")).isEqualTo("error");
    }
}