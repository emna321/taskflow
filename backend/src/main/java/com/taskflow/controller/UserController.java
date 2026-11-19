package com.taskflow.controller;

import com.taskflow.dto.ChangerMotDePasseRequest;
import com.taskflow.dto.MonProfilRequest;
import com.taskflow.dto.RolesRequest;
import com.taskflow.dto.UserRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** CRUD des utilisateurs et attribution des roles : reserve a l'admin. */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Liste des utilisateurs a qui on peut affecter une tache (accessible a tout utilisateur connecte). */
    @GetMapping("/assignables")
    @PreAuthorize("isAuthenticated()")
    public List<UserResponse> assignables() {
        return userService.findAll();
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    /** Attribution des roles : { "roles": ["ADMIN", "COLLABORATEUR"] } */
    @PutMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody RolesRequest request) {
        return userService.updateRoles(id, request.roles());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        userService.delete(id, authentication.getName());
    }

    /** Chacun peut modifier son propre nom/prenom/telephone (pas son email ni ses roles). */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse modifierMonProfil(@Valid @RequestBody MonProfilRequest request, Authentication authentication) {
        User moi = userService.getEntityByEmail(authentication.getName());
        return userService.modifierMonProfil(moi.getId(), request.nom(), request.prenom(), request.tel());
    }

    /** Changement de mot de passe : l'ancien mot de passe est verifie avec BCrypt. */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changerMonMotDePasse(@Valid @RequestBody ChangerMotDePasseRequest request, Authentication authentication) {
        if (!request.nouveau().equals(request.confirmation())) {
            throw new BadRequestException("La confirmation ne correspond pas au nouveau mot de passe");
        }
        User moi = userService.getEntityByEmail(authentication.getName());
        userService.changerMotDePasse(moi.getId(), request.ancien(), request.nouveau());
    }

    /** Photo de profil : chacun peut envoyer la sienne, tout utilisateur connecte peut consulter une photo. */
    @PostMapping("/me/photo")
    @PreAuthorize("isAuthenticated()")
    public UserResponse uploaderMaPhoto(@RequestParam("photo") MultipartFile photo, Authentication authentication) {
        User moi = userService.getEntityByEmail(authentication.getName());
        userService.uploaderPhoto(moi.getId(), photo);
        return userService.findById(moi.getId());
    }

    @GetMapping("/{id}/photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> telechargerPhoto(@PathVariable Long id) {
        User u = userService.getEntity(id);
        if (u.getPhotoContenu() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType type;
        try {
            type = MediaType.parseMediaType(u.getPhotoTypeMime());
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok().contentType(type).body(u.getPhotoContenu());
    }
}
