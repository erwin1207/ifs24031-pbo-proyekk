package org.delcom.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Test Input Valid & Getters")
    void testValidInput() {
        // 1. Arrange
        RegisterForm form = new RegisterForm();
        form.setName("Budi Santoso");
        form.setEmail("budi@delcom.org");
        form.setPassword("rahasia123");

        // 2. Act
        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        // 3. Assert
        assertThat(violations).isEmpty();

        // --- INI YANG DITAMBAHKAN UNTUK MENGHIJAUKAN GETTER ---
        assertThat(form.getName()).isEqualTo("Budi Santoso");
        assertThat(form.getEmail()).isEqualTo("budi@delcom.org");
        assertThat(form.getPassword()).isEqualTo("rahasia123"); // <-- Baris ini yang sebelumnya kurang
    }

    @Test
    @DisplayName("Test Nama Kosong")
    void testNameBlank() {
        RegisterForm form = new RegisterForm();
        form.setName(""); 
        form.setEmail("valid@email.com");
        form.setPassword("pass");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting("message")
                .contains("Nama harus diisi");
    }

    @Test
    @DisplayName("Test Email Kosong")
    void testEmailBlank() {
        RegisterForm form = new RegisterForm();
        form.setName("Budi");
        form.setEmail(null);
        form.setPassword("pass");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting("message")
                .contains("Email harus diisi");
    }

    @Test
    @DisplayName("Test Email Format Salah")
    void testEmailInvalidFormat() {
        RegisterForm form = new RegisterForm();
        form.setName("Budi");
        form.setEmail("bukan-email");
        form.setPassword("pass");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting("message")
                .contains("Format email tidak valid");
    }

    @Test
    @DisplayName("Test Password Kosong")
    void testPasswordBlank() {
        RegisterForm form = new RegisterForm();
        form.setName("Budi");
        form.setEmail("budi@delcom.org");
        form.setPassword("   "); 

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting("message")
                .contains("Kata sandi harus diisi");
    }
}