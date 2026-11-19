package com.taskflow.dto;

import com.taskflow.entity.Priorite;
import com.taskflow.entity.Statut;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TacheRequest(
        @NotBlank(message = "Le titre est obligatoire") String titre,
        String description,
        Statut statut,
        Priorite priorite,
        LocalDate dateEcheance,
        @NotNull(message = "Le projet est obligatoire") Long projetId,
        Long userId) {
}
