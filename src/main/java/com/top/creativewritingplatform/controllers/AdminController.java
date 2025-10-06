package com.top.creativewritingplatform.controllers;

import com.top.creativewritingplatform.DTOs.TextDTO;
import com.top.creativewritingplatform.models.Text;
import com.top.creativewritingplatform.models.TextStatus;
import com.top.creativewritingplatform.models.Topic;
import com.top.creativewritingplatform.services.TextService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TextService textService;

    @GetMapping("/texts")
    public String adminDashboard(@PageableDefault(size = 4, sort = "publicationDate", direction = Sort.Direction.DESC) Pageable pageable,
                                 Model model) {

        Page<Text> pendingTextsPage = textService.getAllPendingTexts(pageable);

        model.addAttribute("pendingTexts", pendingTextsPage.getContent());
        model.addAttribute("currentPage", pendingTextsPage.getNumber());
        model.addAttribute("totalPages", pendingTextsPage.getTotalPages());
        model.addAttribute("totalItems", pendingTextsPage.getTotalElements());

        return "admin/texts";
    }

    @PostMapping("/texts/{id}/approve")
    public String approveText(@PathVariable Long id) {
        textService.updateTextStatus(id, TextStatus.APPROVED);
        return "redirect:/admin/texts";
    }

@PostMapping("/texts/{id}/reject")
public String rejectText(@PathVariable Long id,
                         @RequestParam(required = false) String rejectionReason) {
    textService.rejectText(id, rejectionReason);
    return "redirect:/admin/texts";
}

    @GetMapping("/texts/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Text text = textService.getTextById(id);

        TextDTO textDTO = new TextDTO();
        textDTO.setId(text.getId());
        textDTO.setTitle(text.getTitle());
        textDTO.setContent(text.getContent());
        textDTO.setImageUrl(text.getImageUrl());

        model.addAttribute("textDTO", textDTO);
        model.addAttribute("textId", id);
        return "text/edit";
    }

    @PostMapping("/texts/{id}/edit")
    public String updateText(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(required = false) String topics,
                             HttpServletRequest request) {

        TextDTO textDTO = new TextDTO();
        textDTO.setTitle(title);
        textDTO.setContent(content);

        if (topics != null && !topics.trim().isEmpty()) {
            textDTO.setTopics(Arrays.stream(topics.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }

        textService.updateText(id, textDTO);

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "redirect:/admin/texts?ajaxSuccess=true";
        } else {
            return "redirect:/admin/texts";
        }
    }

    @GetMapping("/texts/{id}/preview-data")
    @ResponseBody
    public Map<String, Object> getTextPreviewData(@PathVariable Long id) {
        Text text = textService.getTextById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("id", text.getId());
        response.put("title", text.getTitle());
        response.put("content", text.getContent());
        response.put("imageUrl", text.getImageUrl());
        response.put("publicationDate", text.getPublicationDate());
        response.put("authorName", text.getAuthor().getFullName());
        response.put("topics", text.getTopics().stream()
                .map(Topic::getName)
                .collect(Collectors.toList()));

        return response;
    }
}