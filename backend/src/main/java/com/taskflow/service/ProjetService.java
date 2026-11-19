package com.taskflow.service;

import com.taskflow.dto.PageResponse;
import com.taskflow.dto.ProjetRequest;
import com.taskflow.dto.ProjetResponse;
import com.taskflow.entity.Projet;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CommentaireRepository;
import com.taskflow.repository.FichierRepository;
import com.taskflow.repository.ProjetRepository;
import com.taskflow.repository.spec.ProjetSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final ClientService clientService;
    private final FichierRepository fichierRepository;
    private final CommentaireRepository commentaireRepository;

    public ProjetService(ProjetRepository projetRepository, ClientService clientService,
                         FichierRepository fichierRepository, CommentaireRepository commentaireRepository) {
        this.projetRepository = projetRepository;
        this.clientService = clientService;
        this.fichierRepository = fichierRepository;
        this.commentaireRepository = commentaireRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjetResponse> findAll(Long clientId) {
        List<Projet> projets = clientId == null ? projetRepository.findAll() : projetRepository.findByClientId(clientId);
        return projets.stream().map(ProjetResponse::from).toList();
    }

    private static final Set<String> TRIS_AUTORISES = Set.of("libelle", "budget", "duree");

    @Transactional(readOnly = true)
    public PageResponse<ProjetResponse> rechercher(String q, Long clientId, String tri, int page, int taille) {
        String proprieteTri = TRIS_AUTORISES.contains(tri) ? tri : "libelle";
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.max(taille, 1), Sort.by(proprieteTri));
        Page<Projet> resultat = projetRepository.findAll(ProjetSpecifications.avecFiltres(q, clientId), pageable);
        return PageResponse.from(resultat.map(ProjetResponse::from));
    }

    @Transactional(readOnly = true)
    public ProjetResponse findById(Long id) {
        return ProjetResponse.from(getEntity(id));
    }

    @Transactional
    public ProjetResponse create(ProjetRequest req) {
        Projet p = new Projet();
        apply(p, req);
        return ProjetResponse.from(projetRepository.save(p));
    }

    @Transactional
    public ProjetResponse update(Long id, ProjetRequest req) {
        Projet p = getEntity(id);
        apply(p, req);
        return ProjetResponse.from(projetRepository.save(p));
    }

    /** Supprime le projet, ses fichiers, et ceux de ses taches (cascade avant suppression des taches). */
    @Transactional
    public void delete(Long id) {
        Projet p = getEntity(id);
        p.getTaches().forEach(t -> {
            fichierRepository.deleteByTacheId(t.getId());
            commentaireRepository.deleteByTacheId(t.getId());
        });
        fichierRepository.deleteByProjetId(p.getId());
        projetRepository.delete(p);
    }

    public Projet getEntity(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable (id=" + id + ")"));
    }

    private void apply(Projet p, ProjetRequest req) {
        p.setLibelle(req.libelle().trim());
        p.setBudget(req.budget());
        p.setDuree(req.duree());
        p.setDescription(req.description());
        // Rattacher le projet a un client
        p.setClient(clientService.getEntity(req.clientId()));
    }
}
