package com.taskflow.dto;

import com.taskflow.entity.Projet;

public record ProjetResponse(Long id, String libelle, Double budget, Integer duree, String description,
                             Long clientId, String clientNom, Integer nbTaches) {

    public static ProjetResponse from(Projet p) {
        return new ProjetResponse(p.getId(), p.getLibelle(), p.getBudget(), p.getDuree(), p.getDescription(),
                p.getClient().getId(), p.getClient().getNom(),
                p.getTaches() == null ? 0 : p.getTaches().size());
    }
}
