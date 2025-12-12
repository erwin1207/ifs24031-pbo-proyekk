package org.delcom.app.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConstUtilTest {

    // --- 1. SOLUSI BARIS 3 MERAH (Class Definition) ---
    @Test
    @DisplayName("Test Constructor (Fix Coverage Class Definition)")
    void testConstructor() {
        // Kita wajib melakukan instansiasi agar Jacoco menandai
        // definisi class (public class ConstUtil) sebagai "Covered"
        ConstUtil constUtil = new ConstUtil();
        assertNotNull(constUtil);
    }

    // --- 2. Test Nilai Konstanta (Memastikan tidak ada typo) ---
    @Test
    @DisplayName("Test Constant Values")
    void testConstantValues() {
        // Cek Key
        assertEquals("AUTH_TOKEN", ConstUtil.KEY_AUTH_TOKEN);
        assertEquals("USER_ID", ConstUtil.KEY_USER_ID);

        // Cek Path Template
        assertEquals("pages/auth/login", ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN);
        assertEquals("pages/auth/register", ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER);
        assertEquals("pages/home", ConstUtil.TEMPLATE_PAGES_HOME);
        assertEquals("pages/health/detail", ConstUtil.TEMPLATE_PAGES_HEALTH_DETAIL);
    }
}