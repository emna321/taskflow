package com.taskflow.repository;

import com.taskflow.dto.FichierMeta;
import com.taskflow.entity.Fichier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FichierRepository extends JpaRepository<Fichier, Long> {

    /** Projection sans la colonne "contenu" : le contenu binaire n'est jamais charge dans une liste. */
    @Query("""
            select new com.taskflow.dto.FichierMeta(f.id, f.nomOriginal, f.typeMime, f.taille, f.dateUpload,
                   f.uploadePar.id, concat(f.uploadePar.prenom, ' ', f.uploadePar.nom))
            from Fichier f where f.tache.id = :tacheId order by f.dateUpload
            """)
    List<FichierMeta> findMetaByTacheId(@Param("tacheId") Long tacheId);

    @Query("""
            select new com.taskflow.dto.FichierMeta(f.id, f.nomOriginal, f.typeMime, f.taille, f.dateUpload,
                   f.uploadePar.id, concat(f.uploadePar.prenom, ' ', f.uploadePar.nom))
            from Fichier f where f.projet.id = :projetId order by f.dateUpload
            """)
    List<FichierMeta> findMetaByProjetId(@Param("projetId") Long projetId);

    void deleteByTacheId(Long tacheId);

    void deleteByProjetId(Long projetId);
}
