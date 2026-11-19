package com.taskflow.dto;

import com.taskflow.entity.Commentaire;

import java.time.LocalDateTime;

public record CommentaireResponse(Long id, String texte, LocalDateTime dateCreation,
                                  Long auteurId, String auteurNom) {

    public static CommentaireResponse from(Commentaire c) {
        return new CommentaireResponse(c.getId(), c.getTexte(), c.getDateCreation(),
                c.getAuteur().getId(), c.getAuteur().getPrenom() + " " + c.getAuteur().getNom());
    }
}
