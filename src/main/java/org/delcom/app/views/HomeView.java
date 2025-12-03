package org.delcom.app.views;

import org.delcom.app.dto.HealthRecordForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.HealthRecordService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeView {

    // Ubah nama variabel service agar tidak bingung
    private final HealthRecordService healthRecordService;

    public HomeView(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @GetMapping
    public String home(Model model) {
        // 1. Cek apakah user sedang login
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        // 2. Masukkan data user yang login ke dalam Model (untuk ditampilkan di HTML)
        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // 3. Ambil daftar Catatan Kesehatan (Records)
        // Kita panggil method getAllHealthRecords yang sudah kita buat di Service
        var records = healthRecordService.getAllHealthRecords(authUser.getId(), "");
        model.addAttribute("records", records); // Di HTML nanti looping pakai variabel 'records'

        // 4. Siapkan Form Kosong untuk Input Data Baru
        model.addAttribute("recordForm", new HealthRecordForm());

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}