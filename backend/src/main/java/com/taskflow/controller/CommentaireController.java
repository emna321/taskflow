package com.taskflow.controller;

import com.taskflow.dto.CommentaireRequest;
import com.taskflow.dto.CommentaireResponse;
import com.taskflow.entity.User;
import com.taskflow.service.CommentaireService;
import com.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Commentaires sur les taches. */
@RestController
public class CommentaireController {

    private final CommentaireService commentaireService;
    private final UserService userService;

    public CommentaireController(CommentaireService commentaireService, UserService userService) {
        this.commentaireService = commentaireService;
        this.userService = userService;
    }

    @GetMapping("/api/taches/{id}/commentaires")
    public List<CommentaireResponse> lister(@PathVariable Long id) {
        return commentaireService.lister(id);
    }

    @PostMapping("/api/taches/{id}/commentaires")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentaireResponse ajouter(@PathVariable Long id, @Valid @RequestBody CommentaireRequest request,
                                       Authentication authentication) {
        User auteur = userService.getEntityByEmail(authentication.getName());
        return commentaireService.ajouter(id, request, auteur);
    }

    @DeleteMapping("/api/commentaires/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, Authentication authentication) {
        User demandeur = userService.getEntityByEmail(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentaireService.supprimer(id, demandeur, isAdmin);
    }
}
