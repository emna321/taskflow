package com.taskflow.service;

import com.taskflow.entity.Commentaire;
import com.taskflow.entity.User;
import com.taskflow.repository.CommentaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentaireServiceTest {

    @Mock private CommentaireRepository commentaireRepository;
    @Mock private TacheService tacheService;

    @InjectMocks
    private CommentaireService commentaireService;

    private User auteur;
    private User autreCollaborateur;
    private Commentaire commentaire;

    @BeforeEach
    void setUp() {
        auteur = new User();
        auteur.setId(1L);
        autreCollaborateur = new User();
        autreCollaborateur.setId(2L);
        commentaire = new Commentaire();
        commentaire.setId(10L);
        commentaire.setAuteur(auteur);
    }

    @Test
    void supprimer_parLAuteur_autorise() {
        when(commentaireRepository.findById(10L)).thenReturn(Optional.of(commentaire));

        assertThatCode(() -> commentaireService.supprimer(10L, auteur, false)).doesNotThrowAnyException();
        verify(commentaireRepository).delete(commentaire);
    }

    @Test
    void supprimer_parUnAdmin_autorise() {
        when(commentaireRepository.findById(10L)).thenReturn(Optional.of(commentaire));

        assertThatCode(() -> commentaireService.supprimer(10L, autreCollaborateur, true)).doesNotThrowAnyException();
        verify(commentaireRepository).delete(commentaire);
    }

    @Test
    void supprimer_parUnAutreCollaborateur_refuse() {
        when(commentaireRepository.findById(10L)).thenReturn(Optional.of(commentaire));

        assertThatThrownBy(() -> commentaireService.supprimer(10L, autreCollaborateur, false))
                .isInstanceOf(AccessDeniedException.class);
        verify(commentaireRepository, never()).delete(any());
    }
}
