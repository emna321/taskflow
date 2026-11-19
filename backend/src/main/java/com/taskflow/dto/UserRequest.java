package com.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Utilise par l'admin pour creer / modifier un utilisateur. Mot de passe facultatif en modification. */
public record UserRequest(
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caracteres") String password,
        @Pattern(regexp = "^$|^[0-9+ ]{8,15}$", message = "Telephone invalide") String tel,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "Le prenom est obligatoire") String prenom,
        Set<String> roles) {
}
