package com.taskflow.service;

import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;
import com.taskflow.repository.TacheRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RappelServiceTest {

    @Mock private TacheRepository tacheRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private RappelService rappelService;

    @Test
    void envoyerRappelsEcheance_marqueRappelEnvoyeEtEnvoieLEmail() {
        Tache t1 = new Tache();
        t1.setId(1L);
        t1.setRappelEnvoye(false);
        Tache t2 = new Tache();
        t2.setId(2L);
        t2.setRappelEnvoye(false);
        LocalDate demain = LocalDate.now().plusDays(1);
        when(tacheRepository.findByDateEcheanceAndStatutNotAndRappelEnvoyeFalseAndUserIsNotNull(demain, Statut.TERMINEE))
                .thenReturn(List.of(t1, t2));

        rappelService.envoyerRappelsEcheance();

        verify(emailService).envoyerRappelEcheance(t1);
        verify(emailService).envoyerRappelEcheance(t2);
        assertThat(t1.isRappelEnvoye()).isTrue();
        assertThat(t2.isRappelEnvoye()).isTrue();

        ArgumentCaptor<List<Tache>> captor = ArgumentCaptor.forClass(List.class);
        verify(tacheRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(t1, t2);
    }

    @Test
    void envoyerRappelsEcheance_aucuneTache_neFaitRien() {
        when(tacheRepository.findByDateEcheanceAndStatutNotAndRappelEnvoyeFalseAndUserIsNotNull(any(), any()))
                .thenReturn(List.of());

        rappelService.envoyerRappelsEcheance();

        verify(emailService, never()).envoyerRappelEcheance(any());
    }
}
