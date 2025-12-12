package org.delcom.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

// Import AssertJ
import static org.assertj.core.api.Assertions.assertThat;

class LoginFormTest {

    private static Validator validator;

    // Inisialisasi Validator Engine sekali saja sebelum semua test
    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Test Input Valid - Tidak boleh ada error")
    void testValidInput() {
        // 1. Arrange
        LoginForm form = new LoginForm();
        form.setEmail("user@delcom.org");
        form.setPassword("rahasia123");
        form.setRememberMe(true);

        // 2. Act (Lakukan Validasi)
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);

        // 3. Assert (Harus kosong / tidak ada pelanggaran)
        assertThat(violations).isEmpty();
        
        // Cek juga getter/setter berfungsi
        assertThat(form.getEmail()).isEqualTo("user@delcom.org");
        assertThat(form.isRememberMe()).isTrue();
    }

    @Test
    @DisplayName("Test Email Kosong - Harus muncul error @NotBlank")
    void testEmailBlank() {
        // 1. Arrange
        LoginForm form = new LoginForm();
        form.setEmail(""); // Kosong
        form.setPassword("pass123");

        // 2. Act
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);

        // 3. Assert
        assertThat(violations).isNotEmpty();
        
        // Ambil pesan errornya saja untuk dicek
        assertThat(violations)
                .extracting("message")
                .contains("Email harus diisi");
    }

    @Test
    @DisplayName("Test Format Email Salah - Harus muncul error @Email")
    void testEmailInvalidFormat() {
        // 1. Arrange
        LoginForm form = new LoginForm();
        form.setEmail("bukan-email-valid"); // Tidak ada @ atau domain
        form.setPassword("pass123");

        // 2. Act
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);

        // 3. Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting("message")
                .contains("Format email tidak valid");
    }

    @Test
    @DisplayName("Test Password Kosong - Harus muncul error @NotBlank")
    void testPasswordBlank() {
        // 1. Arrange
        LoginForm form = new LoginForm();
        form.setEmail("budi@delcom.org");
        form.setPassword(null); // Null atau kosong

        // 2. Act
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);

        // 3. Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting("message")
                .contains("Kata sandi harus diisi");
    }

    @Test
    @DisplayName("Test RememberMe Default - False")
    void testDefaultRememberMe() {
        LoginForm form = new LoginForm();
        // Boolean primitif defaultnya false
        assertThat(form.isRememberMe()).isFalse();
    }
}