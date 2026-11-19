package com.taskflow.dto;

import java.time.LocalDateTime;

/** Metadonnees d'une piece jointe, sans son contenu binaire (jamais charge dans les listes). */
public record FichierMeta(Long id, String nomOriginal, String typeMime, Long taille, LocalDateTime dateUpload,
                          Long uploadeParId, String uploadeParNom) {
}
