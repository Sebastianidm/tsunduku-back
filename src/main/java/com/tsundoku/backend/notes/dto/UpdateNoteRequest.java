package com.tsundoku.backend.notes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Solicitud para actualizar una nota o cita existente")
public class UpdateNoteRequest {

    @Min(value = 0, message = "El número de página no puede ser negativo")
    @Schema(description = "Número de página a la que se ancla la nota", example = "45")
    private Integer pageNumber;

    @Size(max = 5000, message = "El contenido no puede exceder los 5000 caracteres")
    @Schema(description = "Texto libre de la nota o reflexión", example = "Actualizando mi interpretación sobre este pasaje.")
    private String content;

    @Size(max = 2000, message = "La cita no puede exceder los 2000 caracteres")
    @Schema(description = "Fragmento o cita textual del libro (opcional)")
    private String quote;

    @Schema(description = "Indica si la nota contiene spoilers", example = "true")
    private Boolean isSpoiler;

    @Size(max = 255, message = "Las etiquetas no pueden exceder 255 caracteres")
    @Schema(description = "Etiquetas separadas por comas", example = "clave, giro-trama")
    private String tags;
}
