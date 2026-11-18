package com.example.lms.repository;

import com.example.lms.model.Question;
import com.example.lms.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @EntityGraph(attributePaths = "options")
    @Query("select distinct q from Question q order by q.id")
    List<Question> findAllWithOptions();

    List<Question> findByQuiz(Quiz quiz);
}


