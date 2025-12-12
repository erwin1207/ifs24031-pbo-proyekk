package org.delcom.app.configs;

import org.delcom.app.Application;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test URL Publik (/auth/**) - Tidak Boleh Redirect/Block")
    void testPublicUrl_Auth() throws Exception {
        // Karena kamu set .requestMatchers("/auth/**").permitAll()
        // Kita tes akses ke login page. 
        // Jika Controller belum dibuat, expectednya 404 (Not Found).
        // Yang penting BUKAN 302 (Redirect loop) dan BUKAN 403 (Forbidden).
        
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().is(not(403)))
                .andExpect(status().is(not(302))); 
    }

    @Test
    @DisplayName("Test Static Assets (/css/**, /js/**) - Harus Public")
    void testStaticAssets() throws Exception {
        // Akses file CSS dummy
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().is(not(302))) // Tidak boleh dilempar ke login
                .andExpect(status().is(not(403))); // Tidak boleh dilarang
    }

    @Test
    @DisplayName("Test Halaman Private Tanpa Login - Harus Redirect ke /auth/login")
    void testPrivatePage_WithoutLogin_ShouldRedirect() throws Exception {
        // Kamu setting: .anyRequest().authenticated() DAN .formLogin(...)
        // Jadi kalau akses sembarang halaman (misal /dashboard), harusnya dilempar (302) ke login.

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection()) // Harus Redirect
                .andExpect(header().string("Location", containsString("/auth/login"))); // Tujuannya ke login
    }

    @Test
    @DisplayName("Test Halaman Private Dengan Login - Harus Lolos (200/404)")
    @WithMockUser(username = "admin", roles = "ADMIN") // Simulasi User sudah login
    void testPrivatePage_WithLogin_ShouldAccess() throws Exception {
        // Karena sudah login (mock), security tidak boleh redirect lagi.
        // Jika halaman /dashboard belum dibuat controller-nya, dia akan 404.
        // Yang penting statusnya BUKAN 302 dan BUKAN 403.

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is(not(302))) 
                .andExpect(status().is(not(403)));
    }

    @Test
    @DisplayName("Test CSRF Disabled - POST harusnya bisa tanpa Token")
    void testCsrfDisabled() throws Exception {
        // Kamu setting: .csrf(csrf -> csrf.disable())
        // Kita coba POST ke endpoint auth. Jika CSRF nyala, ini butuh token dan akan 403.
        // Karena dimatikan, ini harusnya lolos (bisa 404 atau 405 atau 200, tapi bukan 403 karena CSRF).

        mockMvc.perform(post("/auth/login-process")) // Asumsi endpoint post
                .andExpect(status().is(not(403)));
    }

    @Test
    @DisplayName("Test Logout URL - Harus Redirect ke Login")
    void testLogout() throws Exception {
        // Kamu setting: .logoutSuccessUrl("/auth/login")
        // Default logout di Spring Security itu POST (kecuali CSRF disable, kadang GET bisa, tapi POST lebih aman).
        
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/auth/login")));
    }
}