package com.example.lms.service;

import com.example.lms.model.AnswerOption;
import com.example.lms.model.Question;
import com.example.lms.model.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class QuizScoring {

    private QuizScoring() {
    }

    static int percentage(List<Question> questions, Map<Long, Set<Long>> answers) {
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Quiz has no questions");
        }
        Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());
        if (!questionIds.containsAll(answers.keySet())) {
            throw new IllegalArgumentException("An answer refers to a question outside this quiz");
        }

        int correct = 0;
        for (Question question : questions) {
            Set<Long> selected = answers.getOrDefault(question.getId(), Set.of());
            Set<Long> options = question.getOptions().stream().map(AnswerOption::getId).collect(Collectors.toSet());
            if (!options.containsAll(selected)
                    || (question.getType() == QuestionType.SINGLE_CHOICE && selected.size() > 1)) {
                throw new IllegalArgumentException("Invalid options for question " + question.getId());
            }
            Set<Long> expected = question.getOptions().stream().filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId).collect(Collectors.toSet());
            if (!expected.isEmpty() && expected.equals(selected)) {
                correct++;
            }
        }
        return (int) (100L * correct / questions.size());
    }
}
