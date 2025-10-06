package com.top.creativewritingplatform.controllers;

import com.top.creativewritingplatform.models.PasswordResetToken;
import com.top.creativewritingplatform.models.User;
import com.top.creativewritingplatform.repositories.PasswordResetTokenRepository;
import com.top.creativewritingplatform.repositories.UserRepository;
import com.top.creativewritingplatform.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

@PostMapping("/forgot-password")
public String handleForgotPassword(@RequestParam String email, Model model) {
    try {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = userService.createPasswordResetToken(user);
        userService.sendPasswordResetEmail(user, token);

        model.addAttribute("message", "Письмо отправлено");
    } catch (Exception e) {
        model.addAttribute("error", "Error: " + e.getMessage());
    }
    return "forgot-password";
}
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        System.out.println("=== GET /reset-password ===");
        System.out.println("🔸 Token from URL: " + token);
        System.out.println("🔸 Token length: " + token.length());

        User user = userService.findUserByResetToken(token);
        if (user == null) {
            System.err.println("❌ Token invalid in GET request");
            model.addAttribute("error", "Invalid or expired reset token");
            return "error";
        }

        System.out.println("✅ Token valid, adding to model: " + token);
        model.addAttribute("token", token);
        return "reset-password";
    }
    @PostMapping("/reset-password")
    @Transactional
    public String handleResetPassword(@RequestParam String token,
                                      @RequestParam String password,
                                      Model model) {

        if (token != null && token.contains(",")) {
            token = token.split(",")[0];
        }

        User user = userService.findUserByResetToken(token);
        if (user == null) {
            model.addAttribute("error", "Invalid or expired reset token");
            return "reset-password";
        }

        // Update password
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        PasswordResetToken resetToken = tokenRepository.findByUser(user);
        if (resetToken != null) {
            tokenRepository.delete(resetToken);
            System.out.println("✅ Token deleted successfully");
        }

        model.addAttribute("message", "Password reset successfully! You can now login.");
        return "redirect:/auth/login?resetSuccess=true";
    }
}