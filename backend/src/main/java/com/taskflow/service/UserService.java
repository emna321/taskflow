package com.taskflow.service;

import com.taskflow.dto.UserRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.RoleName;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ConflictException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ActiviteRepository;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.TacheRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** CRUD des utilisateurs et attribution des roles (reserve a l'admin). */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TacheRepository tacheRepository;
    private final PasswordEncoder passwordEncoder;
    private final FichierValidationService fichierValidationService;
    private final ActiviteRepository activiteRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       TacheRepository tacheRepository, PasswordEncoder passwordEncoder,
                       FichierValidationService fichierValidationService,
                       ActiviteRepository activiteRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tacheRepository = tacheRepository;
        this.passwordEncoder = passwordEncoder;
        this.fichierValidationService = fichierValidationService;
        this.activiteRepository = activiteRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getEntity(id));
    }

    @Transactional
    public UserResponse create(UserRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Un compte existe deja avec cet email");
        }
        if (req.password() == null || req.password().isBlank()) {
            throw new BadRequestException("Le mot de passe est obligatoire");
        }
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(req.password()));
        apply(u, req);
        return UserResponse.from(userRepository.save(u));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest req) {
        User u = getEntity(id);
        String email = req.email().trim().toLowerCase();
        if (!email.equals(u.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ConflictException("Un compte existe deja avec cet email");
        }
        u.setEmail(email);
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.password()));
        }
        apply(u, req);
        return UserResponse.from(userRepository.save(u));
    }

    @Transactional
    public UserResponse updateRoles(Long id, Set<String> roles) {
        User u = getEntity(id);
        u.setRoles(resolveRoles(roles));
        return UserResponse.from(userRepository.save(u));
    }

    @Transactional
    public void delete(Long id, String emailConnecte) {
        User u = getEntity(id);
        if (u.getEmail().equalsIgnoreCase(emailConnecte)) {
            throw new BadRequestException("Vous ne pouvez pas supprimer votre propre compte");
        }
        // Les taches affectees deviennent non affectees
        tacheRepository.findByUserId(id).forEach(t -> t.setUser(null));
        // Son historique d'activite reference l'utilisateur (FK NOT NULL) : on le retire
        activiteRepository.deleteByActeurId(id);
        userRepository.delete(u);
    }

    public User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable (id=" + id + ")"));
    }

    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    @Transactional
    public void uploaderPhoto(Long id, MultipartFile fichier) {
        User u = getEntity(id);
        fichierValidationService.validerImage(fichier);
        try {
            u.setPhotoContenu(fichier.getBytes());
        } catch (IOException e) {
            throw new BadRequestException("Impossible de lire le fichier envoye");
        }
        u.setPhotoTypeMime(fichier.getContentType());
        userRepository.save(u);
    }

    @Transactional
    public UserResponse modifierMonProfil(Long id, String nom, String prenom, String tel) {
        User u = getEntity(id);
        u.setNom(nom.trim());
        u.setPrenom(prenom.trim());
        u.setTel(tel);
        return UserResponse.from(userRepository.save(u));
    }

    /** Changement de mot de passe : l'ancien mot de passe doit etre verifie avec BCrypt. */
    @Transactional
    public void changerMotDePasse(Long id, String ancien, String nouveau) {
        User u = getEntity(id);
        if (!passwordEncoder.matches(ancien, u.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }
        u.setPassword(passwordEncoder.encode(nouveau));
        userRepository.save(u);
    }

    private void apply(User u, UserRequest req) {
        u.setNom(req.nom().trim());
        u.setPrenom(req.prenom().trim());
        u.setTel(req.tel());
        if (req.roles() != null && !req.roles().isEmpty()) {
            u.setRoles(resolveRoles(req.roles()));
        } else if (u.getRoles().isEmpty()) {
            u.setRoles(resolveRoles(Set.of(RoleName.COLLABORATEUR.name())));
        }
    }

    private Set<Role> resolveRoles(Set<String> names) {
        Set<Role> roles = new HashSet<>();
        for (String name : names) {
            RoleName rn;
            try {
                rn = RoleName.valueOf(name.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Role inconnu : " + name + " (valeurs : ADMIN, COLLABORATEUR)");
            }
            roles.add(roleRepository.findByLibelle(rn)
                    .orElseThrow(() -> new ResourceNotFoundException("Role introuvable : " + rn)));
        }
        return roles;
    }
}
