package com.taskflow.service;

import com.taskflow.dto.ProjetRequest;
import com.taskflow.dto.ProjetResponse;
import com.taskflow.entity.Projet;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.FichierRepository;
import com.taskflow.repository.ProjetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final ClientService clientService;
    private final FichierRepository fichierRepository;

    public ProjetService(ProjetRepository projetRepository, ClientService clientService,
                         FichierRepository fichierRepository) {
        this.projetRepository = projetRepository;
        this.clientService = clientService;
        this.fichierRepository = fichierRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjetResponse> findAll(Long clientId) {
        List<Projet> projets = clientId == null ? projetRepository.findAll() : projetRepository.findByClientId(clientId);
        return projets.stream().map(ProjetResponse::from).toList();
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
        p.getTaches().forEach(t -> fichierRepository.deleteByTacheId(t.getId()));
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
