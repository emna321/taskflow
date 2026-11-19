package com.taskflow.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record RolesRequest(@NotEmpty(message = "Au moins un role est requis") Set<String> roles) {
}
