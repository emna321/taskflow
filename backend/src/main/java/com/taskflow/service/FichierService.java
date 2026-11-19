package com.taskflow.service;

import com.taskflow.dto.FichierMeta;
import com.taskflow.entity.Fichier;
import com.taskflow.entity.Projet;
import com.taskflow.entity.Tache;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.FichierRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Pieces jointes rattachees a une tache ou un projet. Les listes passent par une projection
 * (FichierMeta) qui ne charge jamais le contenu binaire ; seul le telechargement d'un fichier
 * precis charge son contenu (une seule ligne a la fois).
 */
@Service
public class FichierService {

    private final FichierRepository fichierRepository;
    private final FichierValidationService validationService;
    private final TacheService tacheService;
    private final ProjetService projetService;

    public FichierService(FichierRepository fichierRepository, FichierValidationService validationService,
                          TacheService tacheService, ProjetService projetService) {
        this.fichierRepository = fichierRepository;
        this.validationService = validationService;
        this.tacheService = tacheService;
        this.projetService = projetService;
    }

    @Transactional
    public FichierMeta uploaderPourTache(Long tacheId, MultipartFile fichier, User uploadeur) {
        Tache tache = tacheService.getEntity(tacheId);
        return sauvegarder(fichier, uploadeur, tache, null);
    }

    @Transactional
    public FichierMeta uploaderPourProjet(Long projetId, MultipartFile fichier, User uploadeur) {
        Projet projet = projetService.getEntity(projetId);
        return sauvegarder(fichier, uploadeur, null, projet);
    }

    @Transactional(readOnly = true)
    public List<FichierMeta> listerPourTache(Long tacheId) {
        tacheService.getEntity(tacheId);
        return fichierRepository.findMetaByTacheId(tacheId);
    }

    @Transactional(readOnly = true)
    public List<FichierMeta> listerPourProjet(Long projetId) {
        projetService.getEntity(projetId);
        return fichierRepository.findMetaByProjetId(projetId);
    }

    /** Suppression autorisee a l'ADMIN ou a la personne qui a envoye le fichier. */
    @Transactional
    public void supprimer(Long fichierId, User demandeur, boolean isAdmin) {
        Fichier f = getEntity(fichierId);
        boolean estAuteur = f.getUploadePar().getId().equals(demandeur.getId());
        if (!isAdmin && !estAuteur) {
            throw new AccessDeniedException("Seul l'auteur ou un administrateur peut supprimer ce fichier");
        }
        fichierRepository.delete(f);
    }

    /** Charge le fichier complet (avec son contenu) : utilise uniquement pour un telechargement unitaire. */
    @Transactional(readOnly = true)
    public Fichier getEntity(Long id) {
        return fichierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier introuvable (id=" + id + ")"));
    }

    private FichierMeta sauvegarder(MultipartFile fichier, User uploadeur, Tache tache, Projet projet) {
        String nomNettoye = validationService.validerPieceJointe(fichier);
        Fichier f = new Fichier();
        f.setNomOriginal(nomNettoye);
        f.setTypeMime(fichier.getContentType());
        f.setTaille(fichier.getSize());
        try {
            f.setContenu(fichier.getBytes());
        } catch (IOException e) {
            throw new BadRequestException("Impossible de lire le fichier envoye");
        }
        f.setUploadePar(uploadeur);
        f.setTache(tache);
        f.setProjet(projet);
        Fichier saved = fichierRepository.save(f);
        return new FichierMeta(saved.getId(), saved.getNomOriginal(), saved.getTypeMime(), saved.getTaille(),
                saved.getDateUpload(), uploadeur.getId(), uploadeur.getPrenom() + " " + uploadeur.getNom());
    }
}
