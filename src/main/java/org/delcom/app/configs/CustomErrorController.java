package org.delcom.app.configs;

import org.springframework.boot.web.error.ErrorAttributeOptions;
// PERBAIKAN: Ubah webmvc menjadi web.servlet
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest; // Pastikan ini ServletWebRequest, bukan WebRequest biasa untuk error controller

import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(jakarta.servlet.http.HttpServletRequest request) {
        // PERBAIKAN: Gunakan ServletWebRequest untuk membungkus request
        ServletWebRequest webRequest = new ServletWebRequest(request);
        
        Map<String, Object> attributes = errorAttributes
                .getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        int status = (int) attributes.getOrDefault("status", 500);
        String path = (String) attributes.getOrDefault("path", "unknown");

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status == 500 ? "error" : "fail",
                "error", attributes.getOrDefault("error", "Unknown Error"),
                "message", "Endpoint tidak ditemukan atau terjadi error",
                "path", path);

        return new ResponseEntity<>(body, HttpStatus.valueOf(status));
    }
}