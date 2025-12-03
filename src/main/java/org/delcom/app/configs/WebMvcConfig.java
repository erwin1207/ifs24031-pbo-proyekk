package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // UBAH DARI "/**" MENJADI "/api/**"
                // Artinya: Interceptor hanya aktif untuk jalur API
                .addPathPatterns("/api/**") 
                
                .excludePathPatterns(
                    "/api/auth/**" // Login/Register API tetap boleh lewat
                );
    }
    
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Pastikan folder assets bisa dibaca
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
    }
}