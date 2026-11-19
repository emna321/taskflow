package com.taskflow.repository;

import com.taskflow.entity.Activite;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    List<Activite> findAllByOrderByDateActiviteDesc(Pageable pageable);

    /** Appele avant la suppression d'un utilisateur (FK id_acteur NOT NULL). */
    @Modifying
    @Query("delete from Activite a where a.acteur.id = :userId")
    void deleteByActeurId(@Param("userId") Long userId);
}
