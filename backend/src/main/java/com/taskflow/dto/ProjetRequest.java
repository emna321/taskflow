package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProjetRequest(
        @NotBlank(message = "Le libelle est obligatoire") String libelle,
        @PositiveOrZero(message = "Le budget doit etre positif") Double budget,
        @Positive(message = "La duree doit etre superieure a 0") Integer duree,
        String description,
        @NotNull(message = "Le client est obligatoire") Long clientId) {
}
