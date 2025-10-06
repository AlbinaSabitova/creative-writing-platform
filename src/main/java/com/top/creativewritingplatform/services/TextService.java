package com.top.creativewritingplatform.services;

import com.top.creativewritingplatform.DTOs.TextDTO;
import com.top.creativewritingplatform.exception.TextNotFoundException;
import com.top.creativewritingplatform.models.*;
import com.top.creativewritingplatform.repositories.TextRepository;
import com.top.creativewritingplatform.repositories.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TextService {
    private final TextRepository textRepository;
    private final TopicRepository topicRepository;

        public Page<Text> getAllApprovedTexts(Pageable pageable) {
            return textRepository.findByStatus(TextStatus.APPROVED, pageable);
        }

        public Page<Text> getUserApprovedTexts(User author, Pageable pageable) {
            return textRepository.findByAuthorAndStatus(author, TextStatus.APPROVED, pageable);
        }

        public Page<Text> getAllPendingTexts(Pageable pageable) {
            return textRepository.findByStatus(TextStatus.PENDING, pageable);
        }

        public Page<Text> searchTexts(String query, Pageable pageable) {
            return textRepository.searchApprovedTexts(query, pageable);
        }

        public Page<Text> getTextsByTopic(String topicName, Pageable pageable) {
            return textRepository.findByTopicName(topicName, pageable);
        }

    public Page<Text> getUserPendingTexts(User author, Pageable pageable) {
        return textRepository.findByAuthorAndStatus(author, TextStatus.PENDING, pageable);
    }

    public Page<Text> getUserApprovedTextsNew(User author, Pageable pageable) {
        return textRepository.findTextsByAuthorAndStatus(author, TextStatus.APPROVED, pageable);
    }

    public Page<Text> getUserPendingTextsNew(User author, Pageable pageable) {
        return textRepository.findTextsByAuthorAndStatus(author, TextStatus.PENDING, pageable);
    }

    public Text getTextById(Long id) {
        return textRepository.findById(id)
                .orElseThrow(() -> new TextNotFoundException("Text not found with id: " + id));
    }

    public List<Text> getUserApprovedTextsList(User author) {
        return textRepository.findByAuthorAndStatus(author, TextStatus.APPROVED, Pageable.unpaged()).getContent();
    }

    public List<Text> getUserPendingTextsList(User author) {
        return textRepository.findByAuthorAndStatus(author, TextStatus.PENDING, Pageable.unpaged()).getContent();
    }

    public List<Text> getUserTextsList(User author) {
        return textRepository.findByAuthorOrderByPublicationDateDesc(author);
    }

    @Transactional
    public Text updateTextStatus(Long id, TextStatus status) {
        Text text = getTextById(id);
        text.setStatus(status);
        text.setRejectionReason(null); // Clear rejection reason when approving
        return textRepository.save(text);
    }

    @Transactional
    public Text rejectText(Long id, String rejectionReason) {
        Text text = getTextById(id);
        text.setStatus(TextStatus.REJECTED);
        text.setRejectionReason(rejectionReason);
        return textRepository.save(text);
    }

    @Transactional
    public Text updateText(Long id, TextDTO textDTO) {
        Text text = getTextById(id);

        Set<Topic> topics = textDTO.getTopics().stream()
                .map(topicName -> topicRepository.findByName(topicName)
                        .orElseGet(() -> {
                            Topic newTopic = new Topic();
                            newTopic.setName(topicName);
                            return topicRepository.save(newTopic);
                        }))
                .collect(Collectors.toSet());

        text.setTitle(textDTO.getTitle());
        text.setContent(textDTO.getContent());
        text.setImageUrl(textDTO.getImageUrl());
        text.setTopics(topics);

        return textRepository.save(text);
    }

    @Transactional
    public void toggleLike(Long textId, User user) {
        Text text = getTextById(textId);

        if (text.getLikedBy().contains(user)) {
            text.setLikes(text.getLikes() - 1);
            text.getLikedBy().remove(user);
        } else {
            text.setLikes(text.getLikes() + 1);
            text.getLikedBy().add(user);
        }

        textRepository.save(text);
    }

    public boolean hasUserLikedText(Long textId, User user) {
        Text text = getTextById(textId);
        return text.getLikedBy().contains(user);
    }

    private final List<String> forbiddenWords = Arrays.asList(
            "спам", "насилие"
    );

    private boolean containsForbiddenWords(String content) {
        if (content == null) return false;

        String normalized = content.toLowerCase()
                // Visual similar (homoglyphs)
                .replace('a', 'а').replace('e', 'е').replace('o', 'о')
                .replace('c', 'с').replace('p', 'р').replace('x', 'х')
                .replace('y', 'у').replace('k', 'к').replace('m', 'м')
                .replace('t', 'т').replace('h', 'х').replace('b', 'в')

                // Phonetic equivalents (sound-alike)
                .replace('v', 'в')  // v → в (sound: v)
                .replace('f', 'ф')  // f → ф (sound: f)
                .replace('g', 'г')  // g → г (sound: g)
                .replace('d', 'д')  // d → д (sound: d)
                .replace('z', 'з')  // z → з (sound: z)
                .replace('s', 'с')  // s → с (sound: s)
                .replace('r', 'р')  // r → р (sound: r)
                .replace('l', 'л')  // l → л (sound: l)
                .replace('n', 'н')  // n → н (sound: n)
                .replace('i', 'и')  // i → и (sound: ee)
                .replace('u', 'у')  // u → у (sound: oo)
                .replace('j', 'ж')  // j → ж (sound: zh)
                .replace('w', 'в')  // w → в (sound: v)
                .replace('q', 'к')
                .replace("sh", "ш").replace("ch", "ч").replace("zh", "ж")
                .replace("ya", "я").replace("yu", "ю").replace("yo", "ё")
                ;

        String cleanContent = normalized
                .replaceAll("[^а-яё0-9\\s]", "") // Keep only Cyrillic
                .replaceAll("\\s+", " ");

        return forbiddenWords.stream()
                .anyMatch(word -> cleanContent.contains(word.toLowerCase()));
    }

    @Transactional
    public Text createText(TextDTO textDTO, User author) {
        System.out.println("Author in service: " + (author != null ? author.getEmail() : "NULL"));

        Set<Topic> topics = processTopics(textDTO.getTopics());

        Text text = Text.builder()
                .title(textDTO.getTitle())
                .content(textDTO.getContent())
                .imageUrl(textDTO.getImageUrl())
                .author(author)
                .topics(topics)
                .likes(0)
                .build();

        if (containsForbiddenWords(textDTO.getContent()) || containsForbiddenWords(textDTO.getTitle())) {
            text.setStatus(TextStatus.REJECTED);
            text.setRejectionReason("⚠️Автоматически отклонено. Пожалуйста, исключите запрещённую лексику и отправьте текст снова.");
        } else {
            text.setStatus(TextStatus.PENDING);
        }

        return textRepository.save(text);
    }

    private Set<Topic> processTopics(Set<String> topicNames) {
        Set<Topic> topics = new HashSet<>();
        if (topicNames != null) {
            for (String topicName : topicNames) {
                Topic topic = topicRepository.findByName(topicName)
                        .orElseGet(() -> {
                            Topic newTopic = new Topic();
                            newTopic.setName(topicName);
                            return topicRepository.save(newTopic);
                        });
                topics.add(topic);
            }
        }
        return topics;
    }
}