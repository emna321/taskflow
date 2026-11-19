package com.taskflow.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Mot de passe hache avec BCrypt, jamais stocke en clair. */
    @Column(nullable = false)
    private String password;

    private String tel;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    /** Photo de profil (image seule, 1 Mo max) : stockee en base, jamais chargee dans les listes. */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(length = 2097152)
    private byte[] photoContenu;

    @Column(length = 100)
    private String photoTypeMime;

    /** User - Role : Many-to-Many */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /** User - Tache : One-to-Many (une tache est affectee a un utilisateur) */
    @OneToMany(mappedBy = "user")
    private List<Tache> taches = new ArrayList<>();

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public byte[] getPhotoContenu() { return photoContenu; }
    public void setPhotoContenu(byte[] photoContenu) { this.photoContenu = photoContenu; }
    public String getPhotoTypeMime() { return photoTypeMime; }
    public void setPhotoTypeMime(String photoTypeMime) { this.photoTypeMime = photoTypeMime; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public List<Tache> getTaches() { return taches; }
    public void setTaches(List<Tache> taches) { this.taches = taches; }

    public boolean hasRole(RoleName roleName) {
        return roles.stream().anyMatch(r -> r.getLibelle() == roleName);
    }
}
