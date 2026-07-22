package com.tsundoku.backend.notes.dto;

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
@Schema(description = "Respuesta detallada de una nota o cita anclada")
public class NoteResponse {

    @Schema(description = "ID único de la nota", example = "1")
    private Long id;

    @Schema(description = "ID de la relación UserBook asociada", example = "10")
    private Long userBookId;

    @Schema(description = "Número de página anclada", example = "42")
    private Integer pageNumber;

    @Schema(description = "Contenido o reflexiones de la nota")
    private String content;

    @Schema(description = "Cita o frase resaltada (opcional)")
    private String quote;

    @Schema(description = "Flag indicador de contenido con spoiler", example = "false")
    private Boolean isSpoiler;

    @Schema(description = "Etiquetas asociadas a la nota", example = "reflexiones, capitulo-5")
    private String tags;

    @Schema(description = "Fecha de creación")
    private ZonedDateTime createdAt;

    @Schema(description = "Fecha de última actualización")
    private ZonedDateTime updatedAt;
}
