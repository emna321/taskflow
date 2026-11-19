package com.taskflow.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Evenement du fil d'activite (creation de tache, changement de statut, fichier, commentaire, inscription...). */
@Entity
@Table(name = "activites")
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_acteur", nullable = false)
    private User acteur;

    @Column(nullable = false, length = 255)
    private String action;

    @Column(nullable = false, length = 30)
    private String typeObjet;

    private Long idObjet;

    @Column(nullable = false)
    private LocalDateTime dateActivite = LocalDateTime.now();

    public Activite() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getActeur() { return acteur; }
    public void setActeur(User acteur) { this.acteur = acteur; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTypeObjet() { return typeObjet; }
    public void setTypeObjet(String typeObjet) { this.typeObjet = typeObjet; }
    public Long getIdObjet() { return idObjet; }
    public void setIdObjet(Long idObjet) { this.idObjet = idObjet; }
    public LocalDateTime getDateActivite() { return dateActivite; }
    public void setDateActivite(LocalDateTime dateActivite) { this.dateActivite = dateActivite; }
}
