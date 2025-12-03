package org.delcom.app.utils;

public class ConstUtil {
    // Key untuk penyimpanan Session/Token (Tetap sama)
    public static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";
    public static final String KEY_USER_ID = "USER_ID";

    // Lokasi Template HTML (Jika pakai Thymeleaf/JSP)
    public static final String TEMPLATE_PAGES_AUTH_LOGIN = "pages/auth/login";
    public static final String TEMPLATE_PAGES_AUTH_REGISTER = "pages/auth/register";
    public static final String TEMPLATE_PAGES_HOME = "pages/home";
    
    // Ubah dari "pages/todos/detail" menjadi "pages/health/detail"
    public static final String TEMPLATE_PAGES_HEALTH_DETAIL = "pages/health/detail";
}