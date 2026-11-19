package com.taskflow.service;

import com.taskflow.entity.Fichier;
import com.taskflow.entity.User;
import com.taskflow.repository.FichierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FichierServiceTest {

    @Mock private FichierRepository fichierRepository;
    @Mock private FichierValidationService validationService;
    @Mock private TacheService tacheService;
    @Mock private ProjetService projetService;

    @InjectMocks
    private FichierService fichierService;

    private User auteur;
    private User autreCollaborateur;
    private Fichier fichier;

    @BeforeEach
    void setUp() {
        auteur = new User();
        auteur.setId(1L);

        autreCollaborateur = new User();
        autreCollaborateur.setId(2L);

        fichier = new Fichier();
        fichier.setId(10L);
        fichier.setUploadePar(auteur);
    }

    @Test
    void supprimer_parLAuteur_autorise() {
        when(fichierRepository.findById(10L)).thenReturn(Optional.of(fichier));

        assertThatCode(() -> fichierService.supprimer(10L, auteur, false)).doesNotThrowAnyException();
        verify(fichierRepository).delete(fichier);
    }

    @Test
    void supprimer_parUnAdmin_autoriseMemeSansEtreLAuteur() {
        when(fichierRepository.findById(10L)).thenReturn(Optional.of(fichier));

        assertThatCode(() -> fichierService.supprimer(10L, autreCollaborateur, true)).doesNotThrowAnyException();
        verify(fichierRepository).delete(fichier);
    }

    @Test
    void supprimer_parUnAutreCollaborateur_refuse() {
        when(fichierRepository.findById(10L)).thenReturn(Optional.of(fichier));

        assertThatThrownBy(() -> fichierService.supprimer(10L, autreCollaborateur, false))
                .isInstanceOf(AccessDeniedException.class);
        verify(fichierRepository, never()).delete(any());
    }
}
