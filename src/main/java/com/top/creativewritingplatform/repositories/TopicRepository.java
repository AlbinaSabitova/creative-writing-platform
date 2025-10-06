package com.top.creativewritingplatform.repositories;

import com.top.creativewritingplatform.models.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByName(String name);

//    @Query("SELECT t.name FROM Topic t JOIN t.texts text GROUP BY t.name ORDER BY SUM(text.likes) DESC")
//    List<String> findPopularTopics();

    @Query("SELECT t.name FROM Topic t JOIN t.texts text WHERE text.status = 'APPROVED' GROUP BY t.name ORDER BY SUM(text.likes) DESC")
    List<String> findPopularTopics();
}