package com.taskflow.dto;

import com.taskflow.entity.Priorite;
import com.taskflow.entity.Statut;

import java.util.List;
import java.util.Map;

/** Statistiques agregees pour le tableau de bord. */
public record StatsResponse(
        Map<Statut, Long> tachesParStatut,
        Map<Priorite, Long> tachesParPriorite,
        List<ProjetStat> tachesParProjet,
        long tachesEnRetard,
        List<CollaborateurStat> chargeParCollaborateur,
        long nbClients,
        long nbProjets) {

    public record ProjetStat(Long projetId, String projetLibelle, long nbTaches) {}

    /** Charge = nombre de taches non terminees affectees au collaborateur. */
    public record CollaborateurStat(Long userId, String userNom, long nbTaches) {}
}
