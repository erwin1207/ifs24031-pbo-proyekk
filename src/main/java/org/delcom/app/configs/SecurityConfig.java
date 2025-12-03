package org.delcom.app.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Matikan CSRF biar form tidak error
            
            .authorizeHttpRequests(auth -> auth
                // 1. IZINKAN SEMUA HALAMAN AUTH & ASSETS
                .requestMatchers(
                    "/auth/**",       // Ini mencakup /auth/login/post juga
                    "/api/auth/**", 
                    "/assets/**", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**",
                    "/error",
                    "/favicon.ico"
                ).permitAll()
                
                // 2. SISANYA WAJIB LOGIN
                .anyRequest().authenticated()
            )
            
            // 3. SETTING LOGIN PAGE (Cukup kasih tahu halamannya di mana)
            .formLogin(form -> form
                .loginPage("/auth/login") 
                .permitAll()
                // PENTING: Saya HAPUS bagian .loginProcessingUrl(...)
                // Biar Controller AuthView.java yang menangani prosesnya.
            )
            
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login")
                .permitAll()
            );

        return http.build();
    }
}