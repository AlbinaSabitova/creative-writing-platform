package com.top.creativewritingplatform.services;

import com.top.creativewritingplatform.DTOs.LoginRequest;
import com.top.creativewritingplatform.DTOs.RegisterRequest;
import com.top.creativewritingplatform.DTOs.UserDTO;
import com.top.creativewritingplatform.exception.UserAlreadyExistsException;
import com.top.creativewritingplatform.models.PasswordResetToken;
import com.top.creativewritingplatform.models.Role;
import com.top.creativewritingplatform.models.User;
import com.top.creativewritingplatform.repositories.PasswordResetTokenRepository;
import com.top.creativewritingplatform.repositories.UserRepository;
import com.top.creativewritingplatform.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.url}")
    private String appUrl;

    public User register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Адрес занят");
        }

        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .imageUrl(request.getImageUrl())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        // Convert User to UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        // Generate JWT token with UserDetails
        String jwtToken = jwtService.generateToken(userDetails);

        Cookie cookie = new Cookie("jwtToken", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return savedUser;
    }

    public void authenticateUser(LoginRequest request, HttpServletResponse response) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Неверный логин или пароль");
        }

        // Create UserDetails for JWT generation (consistent with register method)
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        // Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);

        // Set JWT token in cookie
        Cookie cookie = new Cookie("jwtToken", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        response.addCookie(cookie);
    }

    public UserDTO getCurrentUser(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .imageUrl(user.getImageUrl())
                .build();
    }

    public void createAdminIfNotExists() {
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = User.builder()
                    .name("Admin")
                    .surname("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("adminpassword"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
    @Transactional
    public String createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();

        System.out.println("=== CREATING/UPDATING TOKEN ===");
        System.out.println("🔸 For user: " + user.getEmail());
        System.out.println("🔸 New token: " + token);

        // Find existing token
        PasswordResetToken existingToken = tokenRepository.findByUser(user);

        if (existingToken != null) {
            System.out.println("🔄 Updating existing token");
            existingToken.setToken(token);
            existingToken.setExpiryDate(calculateExpiryDate());
            // JPA automatically updates managed entities - no need to call save()
        } else {
            System.out.println("🆕 Creating new token");
            PasswordResetToken newToken = new PasswordResetToken(token, user);
            tokenRepository.save(newToken);
        }

        System.out.println("✅ Token operation completed");
        return token;
    }

    public boolean validateResetToken(String token) {
        System.out.println("=== VALIDATING TOKEN ===");
        System.out.println("🔸 Input token: " + token);

        PasswordResetToken resetToken = tokenRepository.findByToken(token);

        if (resetToken == null) {
            System.err.println("❌ Token not found in database!");

            // Let's see what tokens ARE in the database
            List<PasswordResetToken> allTokens = tokenRepository.findAll();
            System.out.println("🔸 All tokens in DB: " + allTokens.size());
            for (PasswordResetToken t : allTokens) {
                System.out.println("   - Token: " + t.getToken() + " | User: " + t.getUser().getEmail());
            }

            return false;
        }

        System.out.println("✅ Token found in database");
        System.out.println("🔸 Token user: " + resetToken.getUser().getEmail());
        System.out.println("🔸 Token expiry: " + resetToken.getExpiryDate());
        System.out.println("🔸 Current time: " + new Date());
        System.out.println("🔸 Is expired: " + resetToken.isExpired());

        boolean isValid = !resetToken.isExpired();
        System.out.println("🔸 Validation result: " + isValid);

        return isValid;
    }
//@Transactional
//public String createPasswordResetToken(User user) {
//    String token = UUID.randomUUID().toString();
//
//    // Try to find existing token
//    PasswordResetToken existingToken = tokenRepository.findByUser(user);
//
//    if (existingToken != null) {
//        // UPDATE existing token
//        System.out.println("🔄 Updating existing token for user: " + user.getEmail());
//        existingToken.setToken(token);
//        existingToken.setExpiryDate(calculateExpiryDate());
//        // JPA will automatically update this because the entity is managed
//    } else {
//        // INSERT new token
//        System.out.println("🆕 Creating new token for user: " + user.getEmail());
//        PasswordResetToken newToken = new PasswordResetToken(token, user);
//        tokenRepository.save(newToken);
//    }
//
//    System.out.println("✅ Token: " + token);
//    return token;
//}
    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, 1); // 1 hour expiry
        return cal.getTime();
    }

public User findUserByResetToken(String token) {
    System.out.println("=== FIND USER BY TOKEN ===");
    System.out.println("🔸 Looking for token: " + token);

    PasswordResetToken resetToken = tokenRepository.findByToken(token);
    System.out.println("🔸 Token entity found: " + (resetToken != null ? "YES" : "NO"));

    if (resetToken != null) {
        System.out.println("🔸 Token user: " + resetToken.getUser().getEmail());
        System.out.println("🔸 Token expiry: " + resetToken.getExpiryDate());
        System.out.println("🔸 Is expired: " + resetToken.isExpired());

        if (!resetToken.isExpired()) {
            System.out.println("✅ Token is valid, returning user");
            return resetToken.getUser();
        } else {
            System.err.println("❌ Token is expired");
        }
    } else {
        System.err.println("❌ Token not found in database");
    }
    return null;
}
    public void sendPasswordResetEmail(User user, String token) {
        try {
            String resetLink = appUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("albina.sabitova.96@mail.ru");
            message.setTo(user.getEmail());
            message.setSubject("Вы запрашивали обновления пароля");
            message.setText("Для установления нового пароля: " + resetLink);

            System.out.println("🔸 Attempting to send email to: " + user.getEmail());
            System.out.println("🔸 Using SMTP: " + mailSender);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + user.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}