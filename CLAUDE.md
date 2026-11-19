# TaskFlow : instructions pour Claude Code

Projet de formation FullStack (ENSI). Cahiers des charges dans `docs/` : `Cahier_des_charges_TaskFlow.pdf` (le projet) et `CDC_Projects_Manager_prof.pdf` (les étapes demandées par l'enseignante). Le README détaille le lancement et la correspondance avec le CDC.

## Stack
- `backend/` : Spring Boot 3.3, Java 17, Maven, MySQL, Spring Security + JWT (jjwt 0.12), Bean Validation, JavaMail. Package racine `com.taskflow`.
- `frontend/` : Angular 19 standalone, Bootstrap 5 avec la charte Argon Dashboard, routage par hash.
- `ng build` écrit dans `backend/src/main/resources/static/` (ne jamais éditer ce dossier à la main).

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
