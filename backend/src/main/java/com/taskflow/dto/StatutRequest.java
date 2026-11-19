package com.taskflow.dto;

import com.taskflow.entity.Statut;
import jakarta.validation.constraints.NotNull;

public record StatutRequest(@NotNull(message = "Le statut est obligatoire") Statut statut) {
}
