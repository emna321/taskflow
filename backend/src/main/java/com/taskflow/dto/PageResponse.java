package com.taskflow.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Enveloppe de pagination generique (evite de serialiser directement un Page Spring Data,
 * deconseille depuis Spring Boot 3 car PageImpl n'a pas de constructeur par defaut).
 */
public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int page, int taille) {

    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }
}
