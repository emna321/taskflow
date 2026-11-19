package com.taskflow.dto;

import com.taskflow.entity.Activite;

import java.time.LocalDateTime;

public record ActiviteResponse(Long id, String action, String typeObjet, Long idObjet,
                               LocalDateTime dateActivite, String acteurNom) {

    public static ActiviteResponse from(Activite a) {
        return new ActiviteResponse(a.getId(), a.getAction(), a.getTypeObjet(), a.getIdObjet(),
                a.getDateActivite(), a.getActeur().getPrenom() + " " + a.getActeur().getNom());
    }
}
