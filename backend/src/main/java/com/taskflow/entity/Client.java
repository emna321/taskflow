package com.taskflow.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String tel;

    private String email;

    private String adresse;

    /** Logo du client (image seule, 1 Mo max) : stocke en base, jamais charge dans les listes. */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(length = 2097152)
    private byte[] logoContenu;

    @Column(length = 100)
    private String logoTypeMime;

    /** Client - Projet : One-to-Many. Supprimer un client supprime ses projets. */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Projet> projets = new ArrayList<>();

    public Client() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public byte[] getLogoContenu() { return logoContenu; }
    public void setLogoContenu(byte[] logoContenu) { this.logoContenu = logoContenu; }
    public String getLogoTypeMime() { return logoTypeMime; }
    public void setLogoTypeMime(String logoTypeMime) { this.logoTypeMime = logoTypeMime; }
    public List<Projet> getProjets() { return projets; }
    public void setProjets(List<Projet> projets) { this.projets = projets; }
}
