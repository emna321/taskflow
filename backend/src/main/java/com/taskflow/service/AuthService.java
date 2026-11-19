package com.taskflow.service;

import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.RoleName;
import com.taskflow.entity.User;
import com.taskflow.exception.ConflictException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /** Inscription : role COLLABORATEUR par defaut + email a l'admin. */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Un compte existe deja avec cet email");
        }
        Role role = roleRepository.findByLibelle(RoleName.COLLABORATEUR)
                .orElseThrow(() -> new ResourceNotFoundException("Role COLLABORATEUR introuvable"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setTel(req.tel());
        user.setNom(req.nom().trim());
        user.setPrenom(req.prenom().trim());
        user.setRoles(new HashSet<>(Set.of(role)));
        User saved = userRepository.save(user);

        emailService.notifierAdminInscription(saved);
        return UserResponse.from(saved);
    }

    /** Connexion : verifie email / mot de passe puis renvoie un token JWT signe. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password()));
        UserDetails principal = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(principal);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        UserResponse u = UserResponse.from(user);
        return new AuthResponse(token, u.id(), u.email(), u.nom(), u.prenom(), u.roles());
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        return userRepository.findByEmail(email).map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}
