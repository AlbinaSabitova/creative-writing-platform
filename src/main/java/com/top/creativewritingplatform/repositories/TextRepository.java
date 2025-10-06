package com.top.creativewritingplatform.repositories;

import com.top.creativewritingplatform.models.Text;
import com.top.creativewritingplatform.models.TextStatus;
import com.top.creativewritingplatform.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextRepository extends JpaRepository<Text, Long> {

//    @Query("SELECT t.name FROM Topic t JOIN t.texts text GROUP BY t.name ORDER BY SUM(text.likes) DESC")
//    List<String> findPopularTopics();

    Page<Text> findByStatus(TextStatus status, Pageable pageable);
    Page<Text> findByAuthorAndStatus(User author, TextStatus status, Pageable pageable);
    Page<Text> findByAuthor(User author, Pageable pageable);

    List<Text> findByAuthorOrderByPublicationDateDesc(User author);
    @Query("SELECT t FROM Text t WHERE t.status = 'APPROVED' AND " +
            "(LOWER(t.title) LIKE %:query% OR " +
            "LOWER(t.author.name) LIKE %:query% OR " +
            "LOWER(t.author.surname) LIKE %:query%)")
    Page<Text> searchApprovedTexts(@Param("query") String query, Pageable pageable);

    @Query("SELECT t FROM Text t JOIN t.topics topic WHERE topic.name = :topicName AND t.status = 'APPROVED'")
    Page<Text> findByTopicName(@Param("topicName") String topicName, Pageable pageable);

    @Query("SELECT t FROM Text t WHERE t.author = :author AND t.status = :status")
    Page<Text> findTextsByAuthorAndStatus(@Param("author") User author,
                                          @Param("status") TextStatus status,
                                          Pageable pageable);
}