package com.taskflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private RoleName libelle;

    public Role() {}

    public Role(RoleName libelle) {
        this.libelle = libelle;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RoleName getLibelle() { return libelle; }
    public void setLibelle(RoleName libelle) { this.libelle = libelle; }
}
