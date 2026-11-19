package com.taskflow.repository;

import com.taskflow.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long>, JpaSpecificationExecutor<Tache> {
    List<Tache> findByProjetId(Long projetId);
    List<Tache> findByUserId(Long userId);
}
