package com.taskflow.repository;

import com.taskflow.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    List<Commentaire> findByTacheIdOrderByDateCreationAsc(Long tacheId);
    void deleteByTacheId(Long tacheId);
}
