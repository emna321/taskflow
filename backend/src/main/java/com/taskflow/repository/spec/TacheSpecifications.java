package com.taskflow.repository.spec;

import com.taskflow.entity.Priorite;
import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TacheSpecifications {

    private TacheSpecifications() {}

    public static Specification<Tache> avecFiltres(String q, Statut statut, Priorite priorite, Long projetId,
                                                    Long userId, LocalDate dateDebut, LocalDate dateFin) {
        return (root, query, cb) -> {
            List<Predicate> predicats = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicats.add(cb.or(
                        cb.like(cb.lower(root.get("titre")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (statut != null) predicats.add(cb.equal(root.get("statut"), statut));
            if (priorite != null) predicats.add(cb.equal(root.get("priorite"), priorite));
            if (projetId != null) predicats.add(cb.equal(root.get("projet").get("id"), projetId));
            if (userId != null) predicats.add(cb.equal(root.get("user").get("id"), userId));
            if (dateDebut != null) predicats.add(cb.greaterThanOrEqualTo(root.get("dateEcheance"), dateDebut));
            if (dateFin != null) predicats.add(cb.lessThanOrEqualTo(root.get("dateEcheance"), dateFin));
            return cb.and(predicats.toArray(new Predicate[0]));
        };
    }
}
