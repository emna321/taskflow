package com.taskflow.service;

import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;
import com.taskflow.repository.TacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Rappel quotidien par email pour les taches non terminees dont l'echeance est le lendemain. */
@Service
public class RappelService {

    private static final Logger log = LoggerFactory.getLogger(RappelService.class);

    private final TacheRepository tacheRepository;
    private final EmailService emailService;

    public RappelService(TacheRepository tacheRepository, EmailService emailService) {
        this.tacheRepository = tacheRepository;
        this.emailService = emailService;
    }

    /** Tous les jours a 8h. */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void envoyerRappelsEcheance() {
        LocalDate demain = LocalDate.now().plusDays(1);
        List<Tache> aRappeler = tacheRepository
                .findByDateEcheanceAndStatutNotAndRappelEnvoyeFalseAndUserIsNotNull(demain, Statut.TERMINEE);

        for (Tache t : aRappeler) {
            emailService.envoyerRappelEcheance(t);
            t.setRappelEnvoye(true);
        }
        tacheRepository.saveAll(aRappeler);
        if (!aRappeler.isEmpty()) {
            log.info("{} rappel(s) d'echeance envoye(s)", aRappeler.size());
        }
    }
}
