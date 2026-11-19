package com.taskflow.service;

import com.taskflow.dto.ActiviteResponse;
import com.taskflow.entity.Activite;
import com.taskflow.entity.User;
import com.taskflow.repository.ActiviteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Fil d'activite : enregistre et liste les evenements marquants (taches, fichiers, commentaires, inscriptions). */
@Service
public class ActiviteService {

    private final ActiviteRepository activiteRepository;

    public ActiviteService(ActiviteRepository activiteRepository) {
        this.activiteRepository = activiteRepository;
    }

    @Transactional
    public void enregistrer(User acteur, String action, String typeObjet, Long idObjet) {
        Activite a = new Activite();
        a.setActeur(acteur);
        a.setAction(action);
        a.setTypeObjet(typeObjet);
        a.setIdObjet(idObjet);
        activiteRepository.save(a);
    }

    @Transactional(readOnly = true)
    public List<ActiviteResponse> lister(int limite) {
        int taille = Math.min(Math.max(limite, 1), 100);
        return activiteRepository.findAllByOrderByDateActiviteDesc(PageRequest.of(0, taille))
                .stream().map(ActiviteResponse::from).toList();
    }
}
