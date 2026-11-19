package com.taskflow.dto;

import java.util.Set;

public record AuthResponse(String token, Long id, String email, String nom, String prenom, Set<String> roles) {
}
