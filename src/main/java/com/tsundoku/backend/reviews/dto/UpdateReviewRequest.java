package com.tsundoku.backend.reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para actualizar una reseña existente")
public class UpdateReviewRequest {

    @Size(max = 200, message = "El título de la reseña no puede exceder los 200 caracteres")
    @Schema(description = "Título de la reseña")
    private String title;

    @Size(max = 10000, message = "El contenido de la reseña no puede exceder 10,000 caracteres")
    @Schema(description = "Cuerpo de la reseña")
    private String content;

    @Min(value = 1, message = "La calificación mínima es 1 estrella")
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    @Schema(description = "Valoración de 1 a 5 estrellas", example = "4")
    private Integer rating;

    @Schema(description = "Indica si recomienda la lectura del libro", example = "true")
    private Boolean recommend;
}
