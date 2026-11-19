package com.taskflow.repository;

import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long>, JpaSpecificationExecutor<Tache> {
    List<Tache> findByProjetId(Long projetId);
    List<Tache> findByUserId(Long userId);

    /** Taches affectees, non terminees, dont l'echeance est demain et dont le rappel n'a pas encore ete envoye. */
    List<Tache> findByDateEcheanceAndStatutNotAndRappelEnvoyeFalseAndUserIsNotNull(LocalDate dateEcheance, Statut statut);
}
