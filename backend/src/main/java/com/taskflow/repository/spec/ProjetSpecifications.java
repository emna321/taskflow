package com.taskflow.repository.spec;

import com.taskflow.entity.Projet;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProjetSpecifications {

    private ProjetSpecifications() {}

    public static Specification<Projet> avecFiltres(String q, Long clientId) {
        return (root, query, cb) -> {
            List<Predicate> predicats = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicats.add(cb.or(
                        cb.like(cb.lower(root.get("libelle")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (clientId != null) predicats.add(cb.equal(root.get("client").get("id"), clientId));
            return cb.and(predicats.toArray(new Predicate[0]));
        };
    }
}
