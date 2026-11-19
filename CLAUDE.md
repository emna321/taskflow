# TaskFlow : instructions pour Claude Code

Projet de formation FullStack (ENSI). Cahiers des charges dans `docs/` : `Cahier_des_charges_TaskFlow.pdf` (le projet) et `CDC_Projects_Manager_prof.pdf` (les étapes demandées par l'enseignante). Le README détaille le lancement et la correspondance avec le CDC.

## Stack
- `backend/` : Spring Boot 3.3, Java 17, Maven, MySQL (+ PostgreSQL via le profil `render`, voir `application-render.properties`), Spring Security + JWT (jjwt 0.12), Bean Validation, JavaMail. Package racine `com.taskflow`.
- `frontend/` : Angular 19 standalone, Bootstrap 5 avec la charte Argon Dashboard, routage par hash.
- `ng build` écrit dans `backend/src/main/resources/static/` (ne jamais éditer ce dossier à la main).
- Port 8080 est occupé par Oracle XE sur cette machine : le backend tourne sur `server.port=${SERVER_PORT:8089}` et le frontend dev pointe sur `http://localhost:8089/api` (`environment.development.ts`).

## Pieces jointes, logo, photo (fichiers en base)
- Tout fichier (piece jointe de tache/projet, logo client, photo utilisateur) est stocke en base (jamais sur le disque, requis pour Render). Colonnes binaires via `@JdbcTypeCode(SqlTypes.VARBINARY)` (PAS `@Lob` seul, qui basculerait PostgreSQL sur un "large object" OID) : LONGBLOB sur MySQL, bytea sur PostgreSQL.
- Toute requete qui LISTE des fichiers (ou des clients/users) doit passer par une projection JPQL qui exclut la colonne binaire (`FichierRepository.findMetaByTacheId`, `ClientRepository.findAllAsDto`) — ne jamais `findAll()` puis mapper une entite qui porte un `byte[]` dans un DTO de liste.
- Validation centralisee dans `FichierValidationService` (extension + type MIME + signature binaire pour images/PDF + nettoyage du nom de fichier) : reutiliser ce service pour toute nouvelle fonctionnalite de upload plutot que dupliquer la logique.
- `GlobalExceptionHandler` renvoie 413 (`FileTooLargeException` et `MaxUploadSizeExceededException`) en plus des codes existants.

## Recherche paginee et statistiques
- Les endpoints `/recherche` (taches, projets, clients) sont des routes DEDIEES et additives, separees des listes existantes (`GET /api/taches` reste inchange, utilise par dashboard/Kanban/fiche projet). Ne jamais transformer une liste existante en reponse paginee : ca casse tout ce qui consomme encore un simple tableau JSON.
- Filtrage dynamique via `JpaSpecificationExecutor` + classes `*Specifications` dans `repository/spec/`. Reponse paginee enveloppee dans `PageResponse<T>` (jamais `org.springframework.data.domain.Page` serialise directement : deconseille depuis Spring Boot 3, `PageImpl` n'a pas de constructeur par defaut).
- `StatsService` agrege en memoire (taille de donnees modeste pour ce projet de formation) plutot que via des requetes JPQL `GROUP BY` : plus simple a lire, suffisant ici.
- Nouvelle entite = verifier `TacheService.delete()` / `ProjetService.delete()` / `ClientService.delete()` : toute table avec une FK vers Tache/Projet (Fichier, Commentaire) doit etre nettoyee explicitement avant suppression (pas d'association JPA bidirectionnelle pour ces entites, donc pas de cascade automatique).

## Conventions à respecter
- Backend en couches `controller → service → repository`, avec un DTO (record) en entrée et en sortie. Ne jamais exposer une entité JPA ni le mot de passe.
- Droits : `@PreAuthorize("hasRole('ADMIN')")` sur les modifications et suppressions, plus les règles dans `SecurityConfig`. Le collaborateur ne change que le statut de SES tâches (`TacheService.changerStatut`).
- Erreurs : lever `ResourceNotFoundException` (404), `ConflictException` (409), `BadRequestException` (400) ou `AccessDeniedException` (403). `GlobalExceptionHandler` les transforme en JSON.
- Frontend : un service par ressource dans `core/services`. Les droits UI passent par `auth.isAdmin()`, les routes par `authGuard` / `roleGuard`, le token par `jwtInterceptor`. Messages d'erreur via `errorMessage()`.
- Textes de l'interface en français.

## Commandes
- Backend : `cd backend && mvn spring-boot:run` ; tests : `mvn test`
- Frontend : `cd frontend && npx ng serve` ; build : `npx ng build`
- Jar complet : `./build.sh` → `backend/target/taskflow.jar`
- Admin par défaut : admin@taskflow.com / Admin@123

## Après chaque modification
Lancer `mvn test` (backend) et `npx ng build` (frontend). Mettre à jour la collection Postman si une route change.
