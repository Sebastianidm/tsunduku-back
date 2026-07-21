package com.tsundoku.backend.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBookRequest(
        @Size(max = 20, message = "El ISBN no debe superar los 20 caracteres")
        String isbn,

        @NotBlank(message = "El título del libro es obligatorio")
        @Size(max = 255, message = "El título no debe superar los 255 caracteres")
        String title,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 255, message = "El nombre del autor no debe superar los 255 caracteres")
        String author,

        @Size(max = 255, message = "La editorial no debe superar los 255 caracteres")
        String publisher,

        String publishedDate,

        String coverUrl,

        @NotNull(message = "El número total de páginas es obligatorio")
        @Min(value = 1, message = "El libro debe tener al menos 1 página")
        Integer pageCount
) {}
