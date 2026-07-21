package com.tsundoku.backend.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateReadingProgressRequest(
        @NotNull(message = "La página actual es obligatoria")
        @Min(value = 0, message = "La página actual no puede ser negativa")
        Integer currentPage
) {}
