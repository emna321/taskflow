package com.taskflow.dto;

import com.taskflow.entity.Priorite;
import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;

import java.time.LocalDate;

public record TacheResponse(Long id, String titre, String description, Statut statut, Priorite priorite,
                            LocalDate dateEcheance, Long projetId, String projetLibelle,
                            Long userId, String userNomComplet) {

    public static TacheResponse from(Tache t) {
        return new TacheResponse(t.getId(), t.getTitre(), t.getDescription(), t.getStatut(), t.getPriorite(),
                t.getDateEcheance(), t.getProjet().getId(), t.getProjet().getLibelle(),
                t.getUser() == null ? null : t.getUser().getId(),
                t.getUser() == null ? null : t.getUser().getPrenom() + " " + t.getUser().getNom());
    }
}
