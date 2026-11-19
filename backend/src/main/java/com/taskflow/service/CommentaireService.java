package com.taskflow.service;

import com.taskflow.dto.CommentaireRequest;
import com.taskflow.dto.CommentaireResponse;
import com.taskflow.entity.Commentaire;
import com.taskflow.entity.Tache;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CommentaireRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Commentaires sur une tache, du plus ancien au plus recent. */
@Service
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final TacheService tacheService;

    public CommentaireService(CommentaireRepository commentaireRepository, TacheService tacheService) {
        this.commentaireRepository = commentaireRepository;
        this.tacheService = tacheService;
    }

    @Transactional(readOnly = true)
    public List<CommentaireResponse> lister(Long tacheId) {
        tacheService.getEntity(tacheId);
        return commentaireRepository.findByTacheIdOrderByDateCreationAsc(tacheId).stream()
                .map(CommentaireResponse::from).toList();
    }

    @Transactional
    public CommentaireResponse ajouter(Long tacheId, CommentaireRequest req, User auteur) {
        Tache tache = tacheService.getEntity(tacheId);
        Commentaire c = new Commentaire();
        c.setTexte(req.texte().trim());
        c.setAuteur(auteur);
        c.setTache(tache);
        return CommentaireResponse.from(commentaireRepository.save(c));
    }

    /** Suppression autorisee a l'ADMIN ou a l'auteur du commentaire. */
    @Transactional
    public void supprimer(Long id, User demandeur, boolean isAdmin) {
        Commentaire c = commentaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire introuvable (id=" + id + ")"));
        boolean estAuteur = c.getAuteur().getId().equals(demandeur.getId());
        if (!isAdmin && !estAuteur) {
            throw new AccessDeniedException("Seul l'auteur ou un administrateur peut supprimer ce commentaire");
        }
        commentaireRepository.delete(c);
    }
}
