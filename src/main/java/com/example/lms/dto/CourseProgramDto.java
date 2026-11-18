package com.example.lms.dto;

import java.util.List;

public record CourseProgramDto(Long id, String title, List<ModuleDto> modules) {
    public record ModuleDto(Long id, String title, Integer orderIndex, String description,
                            List<LessonDto> lessons) {
    }
}
