package com.taskflow.controller;

import com.taskflow.dto.PageResponse;
import com.taskflow.dto.StatutRequest;
import com.taskflow.dto.TacheRequest;
import com.taskflow.dto.TacheResponse;
import com.taskflow.entity.Priorite;
import com.taskflow.entity.Statut;
import com.taskflow.entity.User;
import com.taskflow.service.ActiviteService;
import com.taskflow.service.TacheService;
import com.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/taches")
public class TacheController {

    private final TacheService tacheService;
    private final UserService userService;
    private final ActiviteService activiteService;

    public TacheController(TacheService tacheService, UserService userService, ActiviteService activiteService) {
        this.tacheService = tacheService;
        this.userService = userService;
        this.activiteService = activiteService;
    }

    /** GET /api/taches, /api/taches?projetId=1 ou /api/taches?userId=2 */
    @GetMapping
    public List<TacheResponse> findAll(@RequestParam(required = false) Long projetId,
                                       @RequestParam(required = false) Long userId) {
        return tacheService.findAll(projetId, userId);
    }

    /** Recherche avancee paginee : GET /api/taches/recherche?q=...&statut=...&page=0&taille=10 */
    @GetMapping("/recherche")
    public PageResponse<TacheResponse> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Statut statut,
            @RequestParam(required = false) Priorite priorite,
            @RequestParam(required = false) Long projetId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "dateEcheance") String tri,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return tacheService.rechercher(q, statut, priorite, projetId, userId, dateDebut, dateFin, tri, page, taille);
    }

    @GetMapping("/{id}")
    public TacheResponse findById(@PathVariable Long id) {
        return tacheService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TacheResponse create(@Valid @RequestBody TacheRequest request, Authentication authentication) {
        TacheResponse cree = tacheService.create(request);
        User acteur = userService.getEntityByEmail(authentication.getName());
        activiteService.enregistrer(acteur, acteur.getPrenom() + " a créé la tâche « " + cree.titre() + " »",
                "TACHE", cree.id());
        return cree;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TacheResponse update(@PathVariable Long id, @Valid @RequestBody TacheRequest request) {
        return tacheService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tacheService.delete(id);
    }

    /** Admin : toutes les taches. Collaborateur : uniquement les siennes (verifie dans le service). */
    @PatchMapping("/{id}/statut")
    public TacheResponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest request,
                                       Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        TacheResponse maj = tacheService.changerStatut(id, request.statut(), authentication.getName(), isAdmin);
        User acteur = userService.getEntityByEmail(authentication.getName());
        String verbe = maj.statut() == com.taskflow.entity.Statut.TERMINEE ? "a terminé" : "a changé le statut de";
        activiteService.enregistrer(acteur, acteur.getPrenom() + " " + verbe + " la tâche « " + maj.titre() + " »",
                "TACHE", maj.id());
        return maj;
    }
}
