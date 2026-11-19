package com.taskflow.service;

import com.taskflow.dto.StatsResponse;
import com.taskflow.entity.*;
import com.taskflow.repository.ClientRepository;
import com.taskflow.repository.ProjetRepository;
import com.taskflow.repository.TacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock private TacheRepository tacheRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private ProjetRepository projetRepository;

    @InjectMocks
    private StatsService statsService;

    private Tache tache(Statut statut, Priorite priorite, LocalDate echeance, User user, Projet projet) {
        Tache t = new Tache();
        t.setStatut(statut);
        t.setPriorite(priorite);
        t.setDateEcheance(echeance);
        t.setUser(user);
        t.setProjet(projet);
        return t;
    }

    @BeforeEach
    void setUp() {
        when(clientRepository.count()).thenReturn(3L);
        when(projetRepository.count()).thenReturn(5L);
    }

    @Test
    void calculer_compteLesTachesEnRetard() {
        Projet projet = new Projet();
        projet.setId(1L);
        projet.setLibelle("Site web");
        User sami = new User();
        sami.setId(1L);
        sami.setNom("Ben Ali");
        sami.setPrenom("Sami");

        LocalDate hier = LocalDate.now().minusDays(1);
        LocalDate demain = LocalDate.now().plusDays(1);
        List<Tache> taches = List.of(
                tache(Statut.EN_COURS, Priorite.HAUTE, hier, sami, projet), // en retard
                tache(Statut.TERMINEE, Priorite.HAUTE, hier, sami, projet), // terminee -> pas en retard
                tache(Statut.A_FAIRE, Priorite.BASSE, demain, sami, projet) // echeance future -> pas en retard
        );
        when(tacheRepository.findAll()).thenReturn(taches);

        StatsResponse stats = statsService.calculer();

        assertThat(stats.tachesEnRetard()).isEqualTo(1);
        assertThat(stats.nbClients()).isEqualTo(3);
        assertThat(stats.nbProjets()).isEqualTo(5);
    }

    @Test
    void calculer_chargeParCollaborateur_excludLesTachesTerminees() {
        Projet projet = new Projet();
        projet.setId(1L);
        projet.setLibelle("Site web");
        User sami = new User();
        sami.setId(1L);
        sami.setNom("Ben Ali");
        sami.setPrenom("Sami");

        List<Tache> taches = List.of(
                tache(Statut.A_FAIRE, Priorite.HAUTE, null, sami, projet),
                tache(Statut.EN_COURS, Priorite.HAUTE, null, sami, projet),
                tache(Statut.TERMINEE, Priorite.HAUTE, null, sami, projet) // ne compte pas dans la charge
        );
        when(tacheRepository.findAll()).thenReturn(taches);

        StatsResponse stats = statsService.calculer();

        assertThat(stats.chargeParCollaborateur()).hasSize(1);
        assertThat(stats.chargeParCollaborateur().get(0).nbTaches()).isEqualTo(2);
        assertThat(stats.chargeParCollaborateur().get(0).userNom()).isEqualTo("Sami Ben Ali");
    }

    @Test
    void calculer_repartitionParStatutEtPriorite() {
        Projet projet = new Projet();
        projet.setId(1L);
        projet.setLibelle("Site web");
        List<Tache> taches = List.of(
                tache(Statut.A_FAIRE, Priorite.BASSE, null, null, projet),
                tache(Statut.A_FAIRE, Priorite.HAUTE, null, null, projet),
                tache(Statut.TERMINEE, Priorite.HAUTE, null, null, projet)
        );
        when(tacheRepository.findAll()).thenReturn(taches);

        StatsResponse stats = statsService.calculer();

        assertThat(stats.tachesParStatut().get(Statut.A_FAIRE)).isEqualTo(2);
        assertThat(stats.tachesParStatut().get(Statut.TERMINEE)).isEqualTo(1);
        assertThat(stats.tachesParPriorite().get(Priorite.HAUTE)).isEqualTo(2);
        assertThat(stats.tachesParProjet()).hasSize(1);
        assertThat(stats.tachesParProjet().get(0).nbTaches()).isEqualTo(3);
    }
}
