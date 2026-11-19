package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

public record MonProfilRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "Le prenom est obligatoire") String prenom,
        String tel) {
}
