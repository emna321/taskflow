package com.taskflow.service;

import com.taskflow.dto.StatsResponse;
import com.taskflow.entity.Priorite;
import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;
import com.taskflow.repository.ClientRepository;
import com.taskflow.repository.ProjetRepository;
import com.taskflow.repository.TacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Agrege les donnees pour le tableau de bord statistiques. */
@Service
public class StatsService {

    private final TacheRepository tacheRepository;
    private final ClientRepository clientRepository;
    private final ProjetRepository projetRepository;

    public StatsService(TacheRepository tacheRepository, ClientRepository clientRepository,
                        ProjetRepository projetRepository) {
        this.tacheRepository = tacheRepository;
        this.clientRepository = clientRepository;
        this.projetRepository = projetRepository;
    }

    @Transactional(readOnly = true)
    public StatsResponse calculer() {
        List<Tache> taches = tacheRepository.findAll();

        Map<Statut, Long> parStatut = new LinkedHashMap<>();
        for (Statut s : Statut.values()) {
            parStatut.put(s, taches.stream().filter(t -> t.getStatut() == s).count());
        }

        Map<Priorite, Long> parPriorite = new LinkedHashMap<>();
        for (Priorite p : Priorite.values()) {
            parPriorite.put(p, taches.stream().filter(t -> t.getPriorite() == p).count());
        }

        List<StatsResponse.ProjetStat> parProjet = taches.stream()
                .collect(Collectors.groupingBy(t -> t.getProjet().getId(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    String libelle = taches.stream().filter(t -> t.getProjet().getId().equals(e.getKey()))
                            .findFirst().map(t -> t.getProjet().getLibelle()).orElse("");
                    return new StatsResponse.ProjetStat(e.getKey(), libelle, e.getValue());
                })
                .sorted(Comparator.comparing(StatsResponse.ProjetStat::projetLibelle))
                .toList();

        LocalDate aujourdhui = LocalDate.now();
        long enRetard = taches.stream()
                .filter(t -> t.getDateEcheance() != null && t.getStatut() != Statut.TERMINEE
                        && t.getDateEcheance().isBefore(aujourdhui))
                .count();

        List<StatsResponse.CollaborateurStat> charge = taches.stream()
                .filter(t -> t.getUser() != null && t.getStatut() != Statut.TERMINEE)
                .collect(Collectors.groupingBy(t -> t.getUser().getId(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    String nom = taches.stream().filter(t -> t.getUser() != null && t.getUser().getId().equals(e.getKey()))
                            .findFirst().map(t -> t.getUser().getPrenom() + " " + t.getUser().getNom()).orElse("");
                    return new StatsResponse.CollaborateurStat(e.getKey(), nom, e.getValue());
                })
                .sorted(Comparator.comparingLong(StatsResponse.CollaborateurStat::nbTaches).reversed())
                .toList();

        return new StatsResponse(parStatut, parPriorite, parProjet, enRetard, charge,
                clientRepository.count(), projetRepository.count());
    }
}
