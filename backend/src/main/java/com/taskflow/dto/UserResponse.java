package com.taskflow.dto;

import com.taskflow.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

/** Le mot de passe n'est jamais renvoye. */
public record UserResponse(Long id, String email, String tel, String nom, String prenom, Set<String> roles) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getTel(), u.getNom(), u.getPrenom(),
                u.getRoles().stream().map(r -> r.getLibelle().name()).collect(Collectors.toSet()));
    }
}
