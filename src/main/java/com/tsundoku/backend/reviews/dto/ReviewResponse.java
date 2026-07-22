package com.tsundoku.backend.reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los detalles de una reseña")
public class ReviewResponse {

    @Schema(description = "ID único de la reseña", example = "1")
    private Long id;

    @Schema(description = "ID de la relación UserBook asociada", example = "10")
    private Long userBookId;

    @Schema(description = "Título del libro asociado", example = "Cien Años de Soledad")
    private String bookTitle;

    @Schema(description = "Título de la reseña", example = "Una obra imponente")
    private String title;

    @Schema(description = "Contenido completo de la reseña")
    private String content;

    @Schema(description = "Calificación (1 a 5)", example = "5")
    private Integer rating;

    @Schema(description = "Indica si se recomienda el libro", example = "true")
    private Boolean recommend;

    @Schema(description = "Fecha de creación de la reseña")
    private ZonedDateTime createdAt;

    @Schema(description = "Fecha de actualización de la reseña")
    private ZonedDateTime updatedAt;
}
