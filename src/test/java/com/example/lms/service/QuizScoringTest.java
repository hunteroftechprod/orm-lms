package com.example.lms.service;

import com.example.lms.model.AnswerOption;
import com.example.lms.model.Question;
import com.example.lms.model.QuestionType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class QuizScoringTest {

    @Test
    void calculatesCorrectSingleChoice() {
        Question question = question(1L, QuestionType.SINGLE_CHOICE, option(10L, true), option(11L, false));
        assertThat(QuizScoring.percentage(List.of(question), Map.of(1L, Set.of(10L)))).isEqualTo(100);
    }

    @Test
    void requiresExactSelectionForMultipleChoice() {
        Question question = question(2L, QuestionType.MULTIPLE_CHOICE,
                option(20L, true), option(21L, true), option(22L, false));
        assertThat(QuizScoring.percentage(List.of(question), Map.of(2L, Set.of(20L)))).isZero();
        assertThat(QuizScoring.percentage(List.of(question), Map.of(2L, Set.of(20L, 21L)))).isEqualTo(100);
    }

    @Test
    void assignsZeroToUnansweredQuestion() {
        Question question = question(1L, QuestionType.SINGLE_CHOICE, option(10L, true));
        assertThat(QuizScoring.percentage(List.of(question), Map.of())).isZero();
    }

    @Test
    void rejectsQuestionOutsideQuiz() {
        Question question = question(1L, QuestionType.SINGLE_CHOICE, option(10L, true));
        assertThatIllegalArgumentException().isThrownBy(() ->
                QuizScoring.percentage(List.of(question), Map.of(2L, Set.of(10L))));
    }

    @Test
    void rejectsOptionOutsideQuestion() {
        Question question = question(1L, QuestionType.SINGLE_CHOICE, option(10L, true));
        assertThatIllegalArgumentException().isThrownBy(() ->
                QuizScoring.percentage(List.of(question), Map.of(1L, Set.of(99L))));
    }

    @Test
    void rejectsTwoOptionsForSingleChoice() {
        Question question = question(1L, QuestionType.SINGLE_CHOICE, option(10L, true), option(11L, false));
        assertThatIllegalArgumentException().isThrownBy(() ->
                QuizScoring.percentage(List.of(question), Map.of(1L, Set.of(10L, 11L))));
    }

    private Question question(Long id, QuestionType type, AnswerOption... options) {
        Question question = new Question();
        question.setId(id);
        question.setType(type);
        question.setOptions(List.of(options));
        return question;
    }

    private AnswerOption option(Long id, boolean correct) {
        AnswerOption option = new AnswerOption();
        option.setId(id);
        option.setCorrect(correct);
        return option;
    }
}
