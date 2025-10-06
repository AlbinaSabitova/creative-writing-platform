package com.top.creativewritingplatform.controllers;

import com.top.creativewritingplatform.models.Text;
import com.top.creativewritingplatform.services.TextService;
import com.top.creativewritingplatform.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final TextService textService;
    private final TopicService topicService;

    @GetMapping("/")
    public String index(@PageableDefault(size = 4, sort = "publicationDate", direction = Sort.Direction.DESC) Pageable pageable,
                        Model model) {
        Page<Text> textPage = textService.getAllApprovedTexts(pageable);

        model.addAttribute("texts", textPage.getContent());
        model.addAttribute("currentPage", textPage.getNumber());
        model.addAttribute("totalPages", textPage.getTotalPages());
        model.addAttribute("totalItems", textPage.getTotalElements());
        model.addAttribute("showPagination", textPage.getTotalPages() > 1);

        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query,
                         @PageableDefault(size = 4, sort = "publicationDate", direction = Sort.Direction.DESC) Pageable pageable,
                         Model model) {

        if (query != null && !query.trim().isEmpty()) {
            Page<Text> resultsPage = textService.searchTexts(query.trim(), pageable);
            model.addAttribute("results", resultsPage.getContent());
            model.addAttribute("query", query);
            model.addAttribute("currentPage", resultsPage.getNumber());
            model.addAttribute("totalPages", resultsPage.getTotalPages());
            model.addAttribute("totalItems", resultsPage.getTotalElements());
            model.addAttribute("showPagination", resultsPage.getTotalPages() > 1);
        } else {
            model.addAttribute("results", Collections.emptyList());
            model.addAttribute("query", "");
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("showPagination", false);
        }

        return "search";
    }

    @GetMapping("/topic")
    public String byTopic(@RequestParam String name,
                          @PageableDefault(size = 4, sort = "publicationDate", direction = Sort.Direction.DESC) Pageable pageable,
                          Model model) {

        Page<Text> textsPage = textService.getTextsByTopic(name, pageable);

        model.addAttribute("texts", textsPage.getContent());
        model.addAttribute("topicName", name);
        model.addAttribute("currentPage", textsPage.getNumber());
        model.addAttribute("totalPages", textsPage.getTotalPages());
        model.addAttribute("totalItems", textsPage.getTotalElements());
        model.addAttribute("showPagination", textsPage.getTotalPages() > 1);

        return "topic-texts";
    }

    @GetMapping("/api/popular-topics")
    @ResponseBody
    public List<String> getPopularTopics() {
        return topicService.getPopularTopics();
    }
}