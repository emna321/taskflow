package com.taskflow.dto;

import com.taskflow.entity.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClientDto(
        Long id,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @Pattern(regexp = "^$|^[0-9+ ]{8,15}$", message = "Telephone invalide") String tel,
        @Email(message = "Email invalide") String email,
        String adresse,
        Integer nbProjets,
        Boolean hasLogo) {

    public static ClientDto from(Client c) {
        return new ClientDto(c.getId(), c.getNom(), c.getTel(), c.getEmail(), c.getAdresse(),
                c.getProjets() == null ? 0 : c.getProjets().size(), c.getLogoContenu() != null);
    }
}
