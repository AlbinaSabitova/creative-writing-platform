package com.top.creativewritingplatform.controllers;

import com.top.creativewritingplatform.DTOs.TextDTO;
import com.top.creativewritingplatform.DTOs.UserDTO;
import com.top.creativewritingplatform.models.Role;
import com.top.creativewritingplatform.models.Text;
import com.top.creativewritingplatform.models.TextStatus;
import com.top.creativewritingplatform.models.User;
import com.top.creativewritingplatform.repositories.TextRepository;
import com.top.creativewritingplatform.repositories.UserRepository;
import com.top.creativewritingplatform.services.TextService;
import com.top.creativewritingplatform.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final TextService textService;
    private final UserRepository userRepository;
    private final TextRepository textRepository;

    @GetMapping("/user/profile")
    public String profile(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDTO userDTO = userService.getCurrentUser(user);

        List<Text> allTexts = textService.getUserTextsList(user);

        model.addAttribute("user", userDTO);
        model.addAttribute("texts", allTexts != null ? allTexts : Collections.emptyList());

        return "user/profile";
    }

    @GetMapping("/text/create")
    public String showCreateTextForm(Model model) {
        model.addAttribute("textDTO", new TextDTO());
        return "text/create";
    }

    @PostMapping("/text/create")
    public String createText(@Valid @ModelAttribute("textDTO") TextDTO textDTO,
                             BindingResult result,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Пожалуйста, исправьте ошибки");
            return "text/create";
        }

        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            textService.createText(textDTO, user);
            redirectAttributes.addFlashAttribute("success", "Текст создан");
            return "redirect:/user/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании текста: " + e.getMessage());
            return "redirect:/text/create";
        }
    }

@GetMapping("/text/view/{id}")
public String viewText(@PathVariable Long id,
                       Authentication authentication,
                       Model model,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {

    Text text = textService.getTextById(id);

    String currentUserEmail = authentication != null ? authentication.getName() : null;
    User currentUser = currentUserEmail != null ?
            userRepository.findByEmail(currentUserEmail).orElse(null) : null;

    boolean canView = text.getStatus() == TextStatus.APPROVED;

    if (currentUser != null) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isAuthor = text.getAuthor().getId().equals(currentUser.getId());
        canView = canView || isAdmin || isAuthor;
    }

    if (!canView) {
        redirectAttributes.addFlashAttribute("error", "Недоступно");
        return "redirect:/";
    }

    Boolean hasLiked = false;
    if (currentUser != null) {
        hasLiked = textService.hasUserLikedText(text.getId(), currentUser);
    }

    String currentUrl = request.getRequestURL().toString();
    model.addAttribute("currentUrl", currentUrl);

    model.addAttribute("text", text);
    model.addAttribute("currentUser", currentUser);
    model.addAttribute("hasLiked", hasLiked);

    return "text/view";
}
    @PostMapping("/text/like")
    public String toggleLike(@RequestParam Long textId, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            textService.toggleLike(textId, user);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось поставить лайк: " + e.getMessage());
        }

        return "redirect:/text/view/" + textId;
    }
}