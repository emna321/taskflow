package com.taskflow.service;

import com.taskflow.dto.TacheRequest;
import com.taskflow.dto.TacheResponse;
import com.taskflow.entity.*;
import com.taskflow.repository.FichierRepository;
import com.taskflow.repository.TacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TacheServiceTest {

    @Mock private TacheRepository tacheRepository;
    @Mock private ProjetService projetService;
    @Mock private UserService userService;
    @Mock private FichierRepository fichierRepository;

    @InjectMocks
    private TacheService tacheService;

    private Projet projet;
    private User sami;
    private Tache tacheDeSami;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setId(1L);
        client.setNom("Client A");

        projet = new Projet();
        projet.setId(1L);
        projet.setLibelle("Site web");
        projet.setClient(client);

        sami = new User();
        sami.setId(5L);
        sami.setEmail("sami@taskflow.com");
        sami.setNom("Ben Ali");
        sami.setPrenom("Sami");

        tacheDeSami = new Tache();
        tacheDeSami.setId(100L);
        tacheDeSami.setTitre("Maquette");
        tacheDeSami.setProjet(projet);
        tacheDeSami.setUser(sami);
    }

    @Test
    void create_valeursParDefautEtAffectation() {
        when(projetService.getEntity(1L)).thenReturn(projet);
        when(userService.getEntity(5L)).thenReturn(sami);
        when(tacheRepository.save(any(Tache.class))).thenAnswer(inv -> inv.getArgument(0));

        TacheResponse r = tacheService.create(
                new TacheRequest("Developper API", null, null, null, LocalDate.of(2026, 12, 1), 1L, 5L));

        assertThat(r.statut()).isEqualTo(Statut.A_FAIRE);
        assertThat(r.priorite()).isEqualTo(Priorite.MOYENNE);
        assertThat(r.userNomComplet()).isEqualTo("Sami Ben Ali");
        assertThat(r.projetLibelle()).isEqualTo("Site web");
    }

    @Test
    void changerStatut_collaborateurSurSaTache_autorise() {
        when(tacheRepository.findById(100L)).thenReturn(Optional.of(tacheDeSami));
        when(tacheRepository.save(tacheDeSami)).thenReturn(tacheDeSami);

        TacheResponse r = tacheService.changerStatut(100L, Statut.EN_COURS, "sami@taskflow.com", false);

        assertThat(r.statut()).isEqualTo(Statut.EN_COURS);
    }

    @Test
    void changerStatut_collaborateurSurTacheDunAutre_refuse() {
        when(tacheRepository.findById(100L)).thenReturn(Optional.of(tacheDeSami));

        assertThatThrownBy(() -> tacheService.changerStatut(100L, Statut.TERMINEE, "autre@taskflow.com", false))
                .isInstanceOf(AccessDeniedException.class);
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void changerStatut_admin_peutChangerToutesLesTaches() {
        when(tacheRepository.findById(100L)).thenReturn(Optional.of(tacheDeSami));
        when(tacheRepository.save(tacheDeSami)).thenReturn(tacheDeSami);

        TacheResponse r = tacheService.changerStatut(100L, Statut.TERMINEE, "admin@taskflow.com", true);

        assertThat(r.statut()).isEqualTo(Statut.TERMINEE);
    }

    @Test
    void delete_supprimeAussiLesFichiersRattaches() {
        when(tacheRepository.findById(100L)).thenReturn(Optional.of(tacheDeSami));

        tacheService.delete(100L);

        verify(fichierRepository).deleteByTacheId(100L);
        verify(tacheRepository).delete(tacheDeSami);
    }

    @Test
    void changerStatut_tacheNonAffectee_collaborateurRefuse() {
        tacheDeSami.setUser(null);
        when(tacheRepository.findById(100L)).thenReturn(Optional.of(tacheDeSami));

        assertThatThrownBy(() -> tacheService.changerStatut(100L, Statut.EN_COURS, "sami@taskflow.com", false))
                .isInstanceOf(AccessDeniedException.class);
    }
}
