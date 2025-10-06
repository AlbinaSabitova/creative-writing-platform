package com.top.creativewritingplatform.services;

import com.top.creativewritingplatform.repositories.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;

    public List<String> getPopularTopics() {
        return topicRepository.findPopularTopics();
    }
}
