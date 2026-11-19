package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentaireRequest(@NotBlank(message = "Le commentaire ne peut pas etre vide") String texte) {
}
