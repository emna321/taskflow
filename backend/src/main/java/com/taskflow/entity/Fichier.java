package com.taskflow.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Piece jointe stockee en base (colonne binaire), rattachee a une Tache OU un Projet (jamais les deux).
 * Mapping portable MySQL (LONGBLOB) / PostgreSQL (bytea) via JdbcTypeCode VARBINARY plutot que @Lob
 * (qui basculerait sur un "large object" PostgreSQL, inadapte ici).
 */
@Entity
@Table(name = "fichiers")
public class Fichier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nomOriginal;

    @Column(nullable = false, length = 150)
    private String typeMime;

    @Column(nullable = false)
    private Long taille;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false, length = 16777216)
    private byte[] contenu;

    @Column(nullable = false)
    private LocalDateTime dateUpload = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_uploade_par", nullable = false)
    private User uploadePar;

    /** Rattachement : exactement un des deux (tache ou projet) doit etre renseigne (verifie par le service). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tache")
    private Tache tache;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_projet")
    private Projet projet;

    public Fichier() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomOriginal() { return nomOriginal; }
    public void setNomOriginal(String nomOriginal) { this.nomOriginal = nomOriginal; }
    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }
    public Long getTaille() { return taille; }
    public void setTaille(Long taille) { this.taille = taille; }
    public byte[] getContenu() { return contenu; }
    public void setContenu(byte[] contenu) { this.contenu = contenu; }
    public LocalDateTime getDateUpload() { return dateUpload; }
    public void setDateUpload(LocalDateTime dateUpload) { this.dateUpload = dateUpload; }
    public User getUploadePar() { return uploadePar; }
    public void setUploadePar(User uploadePar) { this.uploadePar = uploadePar; }
    public Tache getTache() { return tache; }
    public void setTache(Tache tache) { this.tache = tache; }
    public Projet getProjet() { return projet; }
    public void setProjet(Projet projet) { this.projet = projet; }
}
