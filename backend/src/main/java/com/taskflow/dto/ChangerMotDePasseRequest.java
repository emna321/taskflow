package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangerMotDePasseRequest(
        @NotBlank(message = "L'ancien mot de passe est obligatoire") String ancien,
        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 6, message = "Le nouveau mot de passe doit contenir au moins 6 caracteres") String nouveau,
        @NotBlank(message = "La confirmation est obligatoire") String confirmation) {
}
