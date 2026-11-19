package com.taskflow.repository.spec;

import com.taskflow.entity.Client;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ClientSpecifications {

    private ClientSpecifications() {}

    public static Specification<Client> avecRecherche(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            List<Predicate> predicats = List.of(
                    cb.like(cb.lower(root.get("nom")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("tel")), like),
                    cb.like(cb.lower(root.get("adresse")), like));
            return cb.or(predicats.toArray(new Predicate[0]));
        };
    }
}
