package com.example.lms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;
import java.util.Set;

public record QuizSubmissionRequest(@NotNull @Positive Long quizId,
                                    @NotNull @Positive Long studentId,
                                    @NotNull Map<@Positive Long, @NotNull Set<@NotNull @Positive Long>> answers) {
}
