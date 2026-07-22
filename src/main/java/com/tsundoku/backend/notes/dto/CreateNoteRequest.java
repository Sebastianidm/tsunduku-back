package com.tsundoku.backend.notes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Solicitud para crear una nota o cita anclada a una página")
public class CreateNoteRequest {

    @NotNull(message = "El número de página es obligatorio")
    @Min(value = 0, message = "El número de página no puede ser negativo")
    @Schema(description = "Número de página a la que se ancla la nota", example = "42")
    private Integer pageNumber;

    @NotBlank(message = "El contenido de la nota no puede estar vacío")
    @Size(max = 5000, message = "El contenido no puede exceder los 5000 caracteres")
    @Schema(description = "Texto libre de la nota o reflexión", example = "Este capítulo revela la verdadera motivación del protagonista.")
    private String content;

    @Size(max = 2000, message = "La cita no puede exceder los 2000 caracteres")
    @Schema(description = "Fragmento o cita textual del libro (opcional)", example = "El verdadero viaje de descubrimiento no consiste en buscar nuevos paisajes...")
    private String quote;

    @Schema(description = "Indica si la nota contiene spoilers", example = "false")
    private Boolean isSpoiler;

    @Size(max = 255, message = "Las etiquetas no pueden exceder 255 caracteres")
    @Schema(description = "Etiquetas separadas por comas", example = "personajes, revelacion, capitulo-5")
    private String tags;
}
