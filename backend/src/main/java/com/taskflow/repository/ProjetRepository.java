package com.taskflow.repository;

import com.taskflow.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProjetRepository extends JpaRepository<Projet, Long>, JpaSpecificationExecutor<Projet> {
    List<Projet> findByClientId(Long clientId);
}
