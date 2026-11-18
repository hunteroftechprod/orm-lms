package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ModuleRequest(@NotBlank @Size(max = 255) String title,
                            @NotNull @Positive Integer orderIndex,
                            @Size(max = 2000) String description) {
}
