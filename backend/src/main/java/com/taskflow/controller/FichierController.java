package com.taskflow.controller;

import com.taskflow.dto.FichierMeta;
import com.taskflow.entity.Fichier;
import com.taskflow.entity.User;
import com.taskflow.service.ActiviteService;
import com.taskflow.service.FichierService;
import com.taskflow.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.List;

/** Pieces jointes sur les taches et les projets. */
@RestController
public class FichierController {

    private final FichierService fichierService;
    private final UserService userService;
    private final ActiviteService activiteService;

    public FichierController(FichierService fichierService, UserService userService, ActiviteService activiteService) {
        this.fichierService = fichierService;
        this.userService = userService;
        this.activiteService = activiteService;
    }

    @PostMapping("/api/taches/{id}/fichiers")
    @ResponseStatus(HttpStatus.CREATED)
    public FichierMeta uploaderPourTache(@PathVariable Long id, @RequestParam("fichier") MultipartFile fichier,
                                         Authentication authentication) {
        User uploadeur = userService.getEntityByEmail(authentication.getName());
        FichierMeta meta = fichierService.uploaderPourTache(id, fichier, uploadeur);
        activiteService.enregistrer(uploadeur, uploadeur.getPrenom() + " a ajouté le fichier « " + meta.nomOriginal() + " »",
                "TACHE", id);
        return meta;
    }

    @GetMapping("/api/taches/{id}/fichiers")
    public List<FichierMeta> listerPourTache(@PathVariable Long id) {
        return fichierService.listerPourTache(id);
    }

    @PostMapping("/api/projets/{id}/fichiers")
    @ResponseStatus(HttpStatus.CREATED)
    public FichierMeta uploaderPourProjet(@PathVariable Long id, @RequestParam("fichier") MultipartFile fichier,
                                          Authentication authentication) {
        User uploadeur = userService.getEntityByEmail(authentication.getName());
        FichierMeta meta = fichierService.uploaderPourProjet(id, fichier, uploadeur);
        activiteService.enregistrer(uploadeur, uploadeur.getPrenom() + " a ajouté le fichier « " + meta.nomOriginal() + " »",
                "PROJET", id);
        return meta;
    }

    @GetMapping("/api/projets/{id}/fichiers")
    public List<FichierMeta> listerPourProjet(@PathVariable Long id) {
        return fichierService.listerPourProjet(id);
    }

    /** Suppression autorisee a l'ADMIN ou a la personne qui a envoye le fichier (verifie dans le service). */
    @DeleteMapping("/api/fichiers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, Authentication authentication) {
        User demandeur = userService.getEntityByEmail(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        fichierService.supprimer(id, demandeur, isAdmin);
    }

    @GetMapping("/api/fichiers/{id}/telecharger")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        Fichier f = fichierService.getEntity(id);
        String nomEncode = URLEncoder.encode(f.getNomOriginal(), StandardCharsets.UTF_8).replace("+", "%20");
        MediaType type;
        try {
            type = MediaType.parseMediaType(f.getTypeMime());
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + f.getNomOriginal() + "\"; filename*=UTF-8''" + nomEncode)
                .body(f.getContenu());
    }
}
