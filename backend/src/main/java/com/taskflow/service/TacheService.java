package com.taskflow.service;

import com.taskflow.dto.TacheRequest;
import com.taskflow.dto.TacheResponse;
import com.taskflow.entity.Priorite;
import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.FichierRepository;
import com.taskflow.repository.TacheRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ProjetService projetService;
    private final UserService userService;
    private final FichierRepository fichierRepository;

    public TacheService(TacheRepository tacheRepository, ProjetService projetService, UserService userService,
                        FichierRepository fichierRepository) {
        this.tacheRepository = tacheRepository;
        this.projetService = projetService;
        this.userService = userService;
        this.fichierRepository = fichierRepository;
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
        t.setDateEcheance(req.dateEcheance());
        t.setProjet(projetService.getEntity(req.projetId()));
        // Affecter la tache a un utilisateur (facultatif)
        t.setUser(req.userId() == null ? null : userService.getEntity(req.userId()));
    }
}
