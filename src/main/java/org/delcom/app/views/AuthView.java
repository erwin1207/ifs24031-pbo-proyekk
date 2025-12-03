package org.delcom.app.views;

import java.util.List;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/auth")
public class AuthView {

    private final UserService userService;

    public AuthView(UserService userService, AuthTokenService authTokenService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLogin(Model model, HttpSession session) {
        // Cek apakah sudah login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        if (isLoggedIn) {
            return "redirect:/";
        }

        model.addAttribute("loginForm", new LoginForm());
        return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
    }

    @PostMapping("/login/post")
    public String postLogin(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        // Validasi form
        if (bindingResult.hasErrors()) {
            return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        }

        User existingUser = userService.getUserByEmail(loginForm.getEmail());
        if (existingUser == null) {
            bindingResult.rejectValue("email", "error.loginForm", "Pengguna ini belum terdaftar");
            return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        }

        boolean isPasswordMatch = new BCryptPasswordEncoder()
                .matches(loginForm.getPassword(), existingUser.getPassword());
        if (!isPasswordMatch) {
            bindingResult.rejectValue("email", "error.loginForm", "Email atau kata sandi salah");
            return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        }

        // Set authenticated user ke session
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                existingUser,
                null,
                authorities);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext);

        return "redirect:/";
    }

    @GetMapping("/register")
    public String showRegister(Model model, HttpSession session) {
        // Cek apakah sudah login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        if (isLoggedIn) {
            return "redirect:/";
        }

        model.addAttribute("registerForm", new RegisterForm());
        return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
    }

    @PostMapping("/register/post")
    public String postRegister(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Validasi form
        if (bindingResult.hasErrors()) {
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        // Cek apakah email sudah terdaftar
        User existingUser = userService.getUserByEmail(registerForm.getEmail());
        if (existingUser != null) {
            bindingResult.rejectValue("email", "error.registerForm", "Pengguna dengan email ini sudah terdaftar");
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        String hashPassword = new BCryptPasswordEncoder().encode(registerForm.getPassword());

        User createdUser = userService.createUser(
                registerForm.getName(),
                registerForm.getEmail(),
                hashPassword);

        if (createdUser == null) {
            bindingResult.rejectValue("email", "error.registerForm", "Gagal membuat pengguna baru");
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        // 🔥 Kirim pesan sukses pakai Flash Attribute
        redirectAttributes.addFlashAttribute("success", "Akun berhasil dibuat! Silakan login.");

        return "redirect:/auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
