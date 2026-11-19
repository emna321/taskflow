package com.taskflow.service;

import com.taskflow.dto.PageResponse;
import com.taskflow.dto.TacheRequest;
import com.taskflow.dto.TacheResponse;
import com.taskflow.entity.Priorite;
import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CommentaireRepository;
import com.taskflow.repository.FichierRepository;
import com.taskflow.repository.TacheRepository;
import com.taskflow.repository.spec.TacheSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ProjetService projetService;
    private final UserService userService;
    private final FichierRepository fichierRepository;
    private final CommentaireRepository commentaireRepository;

    public TacheService(TacheRepository tacheRepository, ProjetService projetService, UserService userService,
                        FichierRepository fichierRepository, CommentaireRepository commentaireRepository) {
        this.tacheRepository = tacheRepository;
        this.projetService = projetService;
        this.userService = userService;
        this.fichierRepository = fichierRepository;
        this.commentaireRepository = commentaireRepository;
    }

    @Transactional(readOnly = true)
    public List<TacheResponse> findAll(Long projetId, Long userId) {
        List<Tache> taches;
        if (projetId != null) {
            taches = tacheRepository.findByProjetId(projetId);
        } else if (userId != null) {
            taches = tacheRepository.findByUserId(userId);
        } else {
            taches = tacheRepository.findAll();
        }
        return taches.stream().map(TacheResponse::from).toList();
    }

    private static final Set<String> TRIS_AUTORISES = Set.of("titre", "priorite", "statut", "dateEcheance");

    /** Recherche paginee avec filtres (utilisee par la page Taches, en plus de findAll()). */
    @Transactional(readOnly = true)
    public PageResponse<TacheResponse> rechercher(String q, Statut statut, Priorite priorite, Long projetId,
                                                  Long userId, LocalDate dateDebut, LocalDate dateFin,
                                                  String tri, int page, int taille) {
        String proprieteTri = TRIS_AUTORISES.contains(tri) ? tri : "dateEcheance";
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.max(taille, 1), Sort.by(proprieteTri));
        Page<Tache> resultat = tacheRepository.findAll(
                TacheSpecifications.avecFiltres(q, statut, priorite, projetId, userId, dateDebut, dateFin), pageable);
        return PageResponse.from(resultat.map(TacheResponse::from));
    }

    @Transactional(readOnly = true)
    public TacheResponse findById(Long id) {
        return TacheResponse.from(getEntity(id));
    }

    @Transactional
    public TacheResponse create(TacheRequest req) {
        Tache t = new Tache();
        apply(t, req);
        return TacheResponse.from(tacheRepository.save(t));
    }

    @Transactional
    public TacheResponse update(Long id, TacheRequest req) {
        Tache t = getEntity(id);
        apply(t, req);
        return TacheResponse.from(tacheRepository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        Tache t = getEntity(id);
        fichierRepository.deleteByTacheId(t.getId());
        commentaireRepository.deleteByTacheId(t.getId());
        tacheRepository.delete(t);
    }

    /**
     * Changement de statut : l'admin peut changer toutes les taches,
     * le collaborateur uniquement celles qui lui sont affectees.
     */
    @Transactional
    public TacheResponse changerStatut(Long id, Statut statut, String emailConnecte, boolean isAdmin) {
        Tache t = getEntity(id);
        boolean estLaSienne = t.getUser() != null && t.getUser().getEmail().equalsIgnoreCase(emailConnecte);
        if (!isAdmin && !estLaSienne) {
            throw new AccessDeniedException("Vous ne pouvez changer que le statut de vos taches");
        }
        t.setStatut(statut);
        return TacheResponse.from(tacheRepository.save(t));
    }

    public Tache getEntity(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tache introuvable (id=" + id + ")"));
    }

    private void apply(Tache t, TacheRequest req) {
        t.setTitre(req.titre().trim());
        t.setDescription(req.description());
        t.setStatut(req.statut() == null ? Statut.A_FAIRE : req.statut());
        t.setPriorite(req.priorite() == null ? Priorite.MOYENNE : req.priorite());
        // Si l'echeance change, un nouveau rappel doit pouvoir etre envoye pour la nouvelle date
        if (!java.util.Objects.equals(t.getDateEcheance(), req.dateEcheance())) {
            t.setRappelEnvoye(false);
        }
        t.setDateEcheance(req.dateEcheance());
        t.setProjet(projetService.getEntity(req.projetId()));
        // Affecter la tache a un utilisateur (facultatif)
        t.setUser(req.userId() == null ? null : userService.getEntity(req.userId()));
    }
}
