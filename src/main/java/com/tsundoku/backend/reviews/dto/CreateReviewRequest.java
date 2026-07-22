package com.tsundoku.backend.reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para crear una reseña y calificación de un libro")
public class CreateReviewRequest {

    @Size(max = 200, message = "El título de la reseña no puede exceder los 200 caracteres")
    @Schema(description = "Título de la reseña (opcional)", example = "Una obra imprescindible de la literatura contemporánea")
    private String title;

    @NotBlank(message = "El contenido de la reseña no puede estar vacío")
    @Size(max = 10000, message = "El contenido de la reseña no puede exceder 10,000 caracteres")
    @Schema(description = "Cuerpo principal de la reseña y análisis personal", example = "El desarrollo de personajes es sublime...")
    private String content;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1 estrella")
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    @Schema(description = "Valoración de 1 a 5 estrellas", example = "5")
    private Integer rating;

    @Schema(description = "Indica si recomienda la lectura del libro", example = "true")
    private Boolean recommend;
}
