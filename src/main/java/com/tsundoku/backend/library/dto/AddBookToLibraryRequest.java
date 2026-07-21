package com.tsundoku.backend.library.dto;

import com.tsundoku.backend.library.entity.ReadingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddBookToLibraryRequest(
        Long bookId,

        @Valid
        CreateBookRequest createBook,

        @NotNull(message = "El estado de lectura es obligatorio")
        ReadingStatus status,

        @Min(value = 0, message = "La página actual no puede ser negativa")
        Integer currentPage,

        @Min(value = 1, message = "El rating mínimo es 1")
        @Max(value = 5, message = "El rating máximo es 5")
        Integer rating
) {}
