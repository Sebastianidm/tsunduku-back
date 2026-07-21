package com.tsundoku.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato de email no es válido")
        @Size(max = 255, message = "El email no debe exceder 255 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre completo debe tener entre 2 y 100 caracteres")
        String fullName
) {}
