package com.taskflow.config;

import com.taskflow.entity.Client;
import com.taskflow.entity.Priorite;
import com.taskflow.entity.Projet;
import com.taskflow.entity.Role;
import com.taskflow.entity.RoleName;
import com.taskflow.entity.Statut;
import com.taskflow.entity.Tache;
import com.taskflow.entity.User;
import com.taskflow.repository.ClientRepository;
import com.taskflow.repository.ProjetRepository;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.TacheRepository;
import com.taskflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Au demarrage : cree les roles ADMIN / COLLABORATEUR, un premier compte admin,
 * puis - si la base est vide - des clients, projets, taches et collaborateurs de demonstration.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           ClientRepository clientRepository, ProjetRepository projetRepository,
                           TacheRepository tacheRepository, PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (RoleName rn : RoleName.values()) {
            if (roleRepository.findByLibelle(rn).isEmpty()) {
                roleRepository.save(new Role(rn));
            }
        }
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNom("Admin");
            admin.setPrenom("TaskFlow");
            admin.setRoles(new HashSet<>(Set.of(roleRepository.findByLibelle(RoleName.ADMIN).orElseThrow())));
            userRepository.save(admin);
            log.info("Compte admin cree : {}", adminEmail);
        }

        if (clientRepository.count() == 0) {
            seedDemoData();
        }
    }

    private void seedDemoData() {
        log.info("Base vide : creation des donnees de demonstration (clients, projets, taches)");

        Role roleCollab = roleRepository.findByLibelle(RoleName.COLLABORATEUR).orElseThrow();

        List<User> collaborateurs = List.of(
                collaborateur("sarra.bouazizi@taskflow.com", "Bouazizi", "Sarra", roleCollab),
                collaborateur("mehdi.gharbi@taskflow.com", "Gharbi", "Mehdi", roleCollab),
                collaborateur("ines.mansour@taskflow.com", "Mansour", "Ines", roleCollab),
                collaborateur("yassine.trabelsi@taskflow.com", "Trabelsi", "Yassine", roleCollab)
        );

        Client sofrecom = client("Sofrecom Tunisie", "+216 71 862 000", "contact@sofrecom.com", "Immeuble Vermeg, Lac 2, Tunis");
        Client ey = client("EY Tunisie", "+216 71 183 666", "contact@ey.com", "Rue du Lac Windermere, Les Berges du Lac, Tunis");
        Client pwc = client("PwC Tunisie", "+216 71 965 666", "contact@pwc.com", "Immeuble Les Jardins du Lac II, Lac 1, Tunis");
        Client deloitte = client("Deloitte Tunisie", "+216 71 183 500", "contact@deloitte.com", "Rue Lac Léman, Les Berges du Lac, Tunis");
        Client capgemini = client("Capgemini Tunisie", "+216 70 749 000", "contact@capgemini.com", "Centre Urbain Nord, Tunis");
        Client orangeBiz = client("Orange Business Tunisie", "+216 71 199 000", "contact@orange.tn", "Rue du Lac d'Annecy, Les Berges du Lac, Tunis");

        List<Client> clients = List.of(sofrecom, ey, pwc, deloitte, capgemini, orangeBiz);
        clients.forEach(clientRepository::save);

        projetAvecTaches(sofrecom, "Migration plateforme facturation", 85000.0, 120,
                "Refonte de la plateforme de facturation télécom vers une architecture micro-services.",
                collaborateurs.get(0), collaborateurs.get(1),
                new Object[]{"Cadrage et analyse des besoins", Statut.TERMINEE, Priorite.HAUTE, LocalDate.of(2026, 9, 15)},
                new Object[]{"Modélisation des micro-services", Statut.TERMINEE, Priorite.HAUTE, LocalDate.of(2026, 10, 10)},
                new Object[]{"Développement API de facturation", Statut.EN_COURS, Priorite.HAUTE, LocalDate.of(2026, 12, 5)},
                new Object[]{"Tests de montée en charge", Statut.A_FAIRE, Priorite.MOYENNE, LocalDate.of(2026, 12, 20)},
                new Object[]{"Déploiement en production", Statut.A_FAIRE, Priorite.HAUTE, LocalDate.of(2027, 1, 15)});

        projetAvecTaches(sofrecom, "Support supervision réseau", 32000.0, 60,
                "Mise en place d'un tableau de bord de supervision des équipements réseau.",
                collaborateurs.get(2), collaborateurs.get(3),
                new Object[]{"Inventaire des équipements", Statut.TERMINEE, Priorite.MOYENNE, LocalDate.of(2026, 10, 1)},
                new Object[]{"Intégration des sondes de monitoring", Statut.EN_COURS, Priorite.HAUTE, LocalDate.of(2026, 11, 28)},
                new Object[]{"Création des alertes automatiques", Statut.A_FAIRE, Priorite.MOYENNE, LocalDate.of(2026, 12, 10)});

        projetAvecTaches(ey, "Audit des contrôles IT", 54000.0, 45,
                "Audit annuel des contrôles généraux informatiques pour un groupe bancaire.",
                collaborateurs.get(1), collaborateurs.get(2),
                new Object[]{"Collecte de la documentation", Statut.TERMINEE, Priorite.MOYENNE, LocalDate.of(2026, 10, 20)},
                new Object[]{"Tests des contrôles d'accès", Statut.EN_COURS, Priorite.HAUTE, LocalDate.of(2026, 12, 1)},
                new Object[]{"Rédaction du rapport d'audit", Statut.A_FAIRE, Priorite.HAUTE, LocalDate.of(2026, 12, 18)});

        projetAvecTaches(ey, "Mise en conformité RGPD", 41000.0, 75,
                "Accompagnement à la mise en conformité RGPD d'un client grande distribution.",
                collaborateurs.get(0), collaborateurs.get(3),
                new Object[]{"Cartographie des traitements de données", Statut.TERMINEE, Priorite.HAUTE, LocalDate.of(2026, 9, 30)},
                new Object[]{"Analyse d'impact (PIA)", Statut.EN_COURS, Priorite.HAUTE, LocalDate.of(2026, 12, 8)},
                new Object[]{"Plan d'action correctif", Statut.A_FAIRE, Priorite.MOYENNE, LocalDate.of(2027, 1, 20)});

        projetAvecTaches(pwc, "Transformation digitale finance", 95000.0, 150,
                "Accompagnement à la refonte des processus financiers et au déploiement d'un ERP.",
                collaborateurs.get(2), collaborateurs.get(0),
                new Object[]{"Diagnostic des processus existants", Statut.TERMINEE, Priorite.MOYENNE, LocalDate.of(2026, 9, 25)},
                new Object[]{"Paramétrage du module comptabilité", Statut.EN_COURS, Priorite.HAUTE, LocalDate.of(2026, 12, 15)},
                new Object[]{"Formation des utilisateurs clés", Statut.A_FAIRE, Priorite.MOYENNE, LocalDate.of(2027, 1, 10)},
                new Object[]{"Bascule en production", Statut.A_FAIRE, Priorite.HAUTE, LocalDate.of(2027, 2, 1)});

        projetAvecTaches(deloitte, "Stratégie cybersécurité groupe", 68000.0, 90,
                "Élaboration d'une feuille de route cybersécurité pour un groupe industriel régional.",
                collaborateurs.get(3), collaborateurs.get(1),
                new Object[]{"Évaluation de la maturité sécurité", Statut.TERMINEE, Priorite.HAUTE, LocalDate.of(2026, 10, 12)},
                new Object[]{"Atelier de cartographie des risques", Statut.EN_COURS, Priorite.HAUTE, LocalDate.of(2026, 12, 3)},
                new Object[]{"Rédaction de la feuille de route", Statut.A_FAIRE, Priorite.MOYENNE, LocalDate.of(2027, 1, 5)});

        projetAvecTaches(capgemini, "Développement application mobile RH", 47000.0, 100,
                "Conception et développement d'une application mobile de gestion RH pour les collaborateurs.",
                collaborateurs.get(1), collaborateurs.get(3),
                new Object[]{"Maquettage UX/UI", Statut.TERMINEE, Priorite.MOYENNE, LocalDate.of(2026, 9, 18)},
                new Object[]{"Développement module congés", Statut.EN_COURS, Priorite.HAUTE, LocalDate.of(2026, 12, 2)},
                new Object[]{"Développement module notes de frais", Statut.A_FAIRE, Priorite.MOYENNE, LocalDate.of(2026, 12, 22)},
                new Object[]{"Recette et mise en production", Statut.A_FAIRE, Priorite.HAUTE, LocalDate.of(2027, 1, 25)});

        projetAvecTaches(orangeBiz, "Déploiement infrastructure cloud hybride", 72000.0, 110,
                "Migration des serveurs on-premise vers une infrastructure cloud hybride sécurisée.",
                collaborateurs.get(0), collaborateurs.get(2),
                new Object[]{"Étude de faisabilité technique", Statut.TERMINEE, Priorite.HAUTE, LocalDate.of(2026, 9, 10)},
                new Object[]{"Migration des serveurs pilotes", Statut.EN_COURS, Priorite.HAUTE, LocalDate.of(2026, 11, 30)},
                new Object[]{"Sécurisation des accès VPN", Statut.A_FAIRE, Priorite.HAUTE, LocalDate.of(2026, 12, 14)});

        log.info("Donnees de demonstration creees : {} clients, {} collaborateurs", clients.size(), collaborateurs.size());
    }

    private User collaborateur(String email, String nom, String prenom, Role roleCollab) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Collab@123"));
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setRoles(new HashSet<>(Set.of(roleCollab)));
        return userRepository.save(user);
    }

    private Client client(String nom, String tel, String email, String adresse) {
        Client c = new Client();
        c.setNom(nom);
        c.setTel(tel);
        c.setEmail(email);
        c.setAdresse(adresse);
        return c;
    }

    /** Chaque element de taches est {titre, Statut, Priorite, LocalDate}, affecte alternativement aux deux collaborateurs donnes. */
    private void projetAvecTaches(Client client, String libelle, Double budget, Integer duree, String description,
                                   User collab1, User collab2, Object[]... taches) {
        Projet projet = new Projet();
        projet.setLibelle(libelle);
        projet.setBudget(budget);
        projet.setDuree(duree);
        projet.setDescription(description);
        projet.setClient(client);
        projetRepository.save(projet);

        for (int i = 0; i < taches.length; i++) {
            Object[] t = taches[i];
            Tache tache = new Tache();
            tache.setTitre((String) t[0]);
            tache.setStatut((Statut) t[1]);
            tache.setPriorite((Priorite) t[2]);
            tache.setDateEcheance((LocalDate) t[3]);
            tache.setProjet(projet);
            tache.setUser(i % 2 == 0 ? collab1 : collab2);
            tacheRepository.save(tache);
        }
    }
}
