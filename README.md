# TaskFlow : gestion de clients, projets et tâches

Application web **Spring Boot 3 + Angular 19**, réalisée dans le cadre de la formation FullStack (ENSI).
Elle regroupe clients, projets et tâches, avec deux rôles (**ADMIN** et **COLLABORATEUR**), une authentification **JWT** et un email envoyé à l'admin à chaque inscription.

```
taskflow/
├── backend/        API REST Spring Boot (Java 17, Maven, MySQL, Spring Security + JWT)
├── frontend/       Application Angular (Components, Services, Guards, Interceptor)
├── postman/        Collection Postman (43 requêtes : CRUD, login, register, droits)
├── docs/           Cahiers des charges + diagramme de classes (PlantUML)
├── deploy/         Service systemd, variables de prod, script de déploiement VPS
└── build.sh        Construit taskflow.jar (Angular dans static/ + Spring Boot)
```

---

## 1. Prérequis

| Outil | Version | Vérifier |
|---|---|---|
| JDK | 17 ou plus | `java -version` |
| Maven | 3.9+ (ou le Maven intégré de VS Code / IntelliJ) | `mvn -v` |
| Node.js | 20 ou 22 | `node -v` |
| MySQL | 8 (XAMPP/WAMP fonctionne aussi) | `mysql --version` |

Extensions VS Code recommandées (proposées automatiquement à l'ouverture du dossier) : *Extension Pack for Java*, *Spring Boot Extension Pack*, *Angular Language Service*, *Claude Code*.

---

## 2. Lancer le projet en local

### Backend (port 8080)
1. Démarrer MySQL. La base `taskflow_db` est créée automatiquement.
2. Si votre utilisateur MySQL n'est pas `root` sans mot de passe, modifiez `backend/src/main/resources/application.properties` (ou définissez `DB_USER` / `DB_PASSWORD`).
3. Lancer :
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Au premier démarrage, les rôles et le compte admin sont créés :
   **admin@taskflow.com / Admin@123**

### Frontend (port 4200)
```bash
cd frontend
npm install
npx ng serve
```
Ouvrir http://localhost:4200. En développement, Angular appelle `http://localhost:8080/api` (CORS autorisé uniquement pour `http://localhost:4200`).

---

## 3. Correspondance avec le cahier des charges

| Étape (CDC de la prof) | Où c'est fait |
|---|---|
| 1. Projet Spring Boot (Web, Data JPA, DevTools, MySQL) | `backend/pom.xml` |
| 2. CRUD des entités | `entity/`, `repository/`, `service/`, `controller/` + DTO dans `dto/` |
| 3. Tests Postman | `postman/TaskFlow.postman_collection.json` |
| 4. Couche Security + JWT | `security/` (JwtService, JwtAuthFilter, SecurityConfig, BCrypt) |
| 5. Email à l'admin après inscription | `service/EmailService.java` |
| 6–7. Tests login / register / ressources sécurisées | Dossiers 1 et 6 de la collection Postman |
| 8. Composants + navbar (Clients, Projets, Tâches, Users) | `frontend/src/app/pages/`, `layout/` |
| 9. Guard + Interceptor | `core/guards/`, `core/interceptors/jwt.interceptor.ts` |
| 10. Prise en compte des rôles | `roleGuard`, boutons masqués selon `auth.isAdmin()`, `@PreAuthorize` côté API |
| 11. Build `taskflow.jar` avec Angular dans `static/` | `build.sh` / `build.ps1` |
| 12. Déploiement VPS | `deploy/` (section 7) |
| Tests unitaires JUnit (couche service) | `backend/src/test/java/.../service/` |

### Droits par rôle

| Module | Admin | Collaborateur |
|---|---|---|
| Clients / Projets : lister, consulter, ajouter | ✅ | ✅ |
| Clients / Projets : modifier, supprimer | ✅ | ❌ (403) |
| Tâches : lister, consulter, ajouter, affecter | ✅ | ✅ |
| Tâches : modifier, supprimer | ✅ | ❌ (403) |
| Tâches : changer le statut | toutes | seulement les siennes |
| Utilisateurs : CRUD + rôles | ✅ | ❌ (403, page bloquée par le Guard) |
| Pièces jointes (tâches/projets) : envoyer, lister, télécharger | ✅ | ✅ |
| Pièces jointes : supprimer | ✅ | seulement les siennes (403 sinon) |
| Logo client / photo de profil : envoyer, consulter | ✅ | ✅ (sa propre photo) |

À l'inscription, l'utilisateur reçoit le rôle **COLLABORATEUR**. L'admin peut ensuite le promouvoir depuis la page *Utilisateurs*.

---

## 4. API REST

Toutes les routes sauf `login` et `register` exigent l'en-tête `Authorization: Bearer <token>`.

| Méthode | Route | Rôle |
|---|---|---|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public → `{ token, id, email, nom, prenom, roles }` |
| GET | `/api/auth/me` | connecté |
| GET / POST | `/api/clients`, `GET /api/clients/{id}` | connecté |
| PUT / DELETE | `/api/clients/{id}` | ADMIN |
| GET / POST | `/api/projets` (`?clientId=`), `GET /api/projets/{id}` | connecté |
| PUT / DELETE | `/api/projets/{id}` | ADMIN |
| GET / POST | `/api/taches` (`?projetId=` / `?userId=`), `GET /api/taches/{id}` | connecté |
| PUT / DELETE | `/api/taches/{id}` | ADMIN |
| PATCH | `/api/taches/{id}/statut` `{ "statut": "EN_COURS" }` | ADMIN, ou collaborateur sur sa tâche |
| GET | `/api/users/assignables` | connecté (liste pour affecter une tâche) |
| GET / POST / PUT / DELETE | `/api/users`, `/api/users/{id}` | ADMIN |
| PUT | `/api/users/{id}/roles` `{ "roles": ["ADMIN"] }` | ADMIN |
| POST (multipart) | `/api/taches/{id}/fichiers` (champ `fichier`) | connecté |
| GET | `/api/taches/{id}/fichiers` | connecté |
| POST (multipart) | `/api/projets/{id}/fichiers` (champ `fichier`) | connecté |
| GET | `/api/projets/{id}/fichiers` | connecté |
| DELETE | `/api/fichiers/{id}` | ADMIN ou l'auteur de l'envoi |
| GET | `/api/fichiers/{id}/telecharger` | connecté |
| POST (multipart) | `/api/clients/{id}/logo` (champ `logo`, image 1 Mo max) | connecté |
| GET | `/api/clients/{id}/logo` | connecté |
| POST (multipart) | `/api/users/me/photo` (champ `photo`, image 1 Mo max) | connecté |
| GET | `/api/users/{id}/photo` | connecté |

**Erreurs** : réponse JSON `{ status, message, details }` avec les codes 400 (validation ou fichier invalide, avec un message par champ), 401 (non connecté / token invalide), 403 (droits insuffisants), 404 (introuvable), 409 (email déjà utilisé) et 413 (fichier trop volumineux).

**Pièces jointes** : stockées en base (colonne binaire LONGBLOB sur MySQL / bytea sur PostgreSQL), jamais sur le disque — indispensable sur Render dont le plan gratuit ne conserve pas le filesystem. Types acceptés : pdf, doc(x), xls(x), ppt(x), txt, csv, png, jpg, jpeg, webp, zip (5 Mo max, 1 Mo pour le logo/la photo). L'extension, le type MIME et la signature binaire (pour les images et PDF) sont vérifiés côté serveur ; le nom de fichier est nettoyé (chemins et caractères spéciaux retirés).

---

## 5. Tests

**JUnit** (couche service : `ClientService`, `TacheService` dont les règles de statut, `AuthService` dont BCrypt et l'email) :
```bash
cd backend && mvn test
```

**Postman** : *Import* → `postman/TaskFlow.postman_collection.json`, puis *Run collection* (backend démarré). Les tokens admin et collaborateur sont enregistrés automatiquement par les requêtes de login. Le dossier *6. Sécurité et droits* vérifie les 401 et 403.

---

## 6. Email à l'admin

Par défaut (`app.mail.enabled=false`), l'email est seulement **affiché dans la console** du backend : pratique pour la démo. Pour un envoi réel avec Gmail :
1. Activez la validation en 2 étapes, puis créez un *mot de passe d'application* (Compte Google → Sécurité).
2. Définissez `MAIL_ENABLED=true`, `MAIL_USERNAME=vous@gmail.com`, `MAIL_PASSWORD=<mot de passe d'application>` et `ADMIN_EMAIL=<email qui reçoit les notifications>`.

---

## 7. Build `taskflow.jar` et déploiement VPS

### Build
```bash
./build.sh          # Linux / macOS / WSL
./build.ps1         # Windows PowerShell
java -jar backend/target/taskflow.jar   # → http://localhost:8080
```
`ng build` écrit directement dans `backend/src/main/resources/static/`, puis Maven embarque le tout dans le jar. L'application utilise le routage Angular par hash (`/#/clients`) : aucune configuration serveur supplémentaire n'est nécessaire.

### Sur le VPS (Ubuntu), une seule fois
```bash
sudo apt update && sudo apt install -y openjdk-17-jre-headless mysql-server
sudo mysql -e "CREATE DATABASE taskflow_db; CREATE USER 'taskflow'@'localhost' IDENTIFIED BY 'change_moi'; GRANT ALL ON taskflow_db.* TO 'taskflow'@'localhost';"
sudo useradd -r -s /usr/sbin/nologin taskflow
sudo mkdir -p /opt/taskflow && sudo chown taskflow:taskflow /opt/taskflow
# copier deploy/taskflow.env.example vers /opt/taskflow/taskflow.env et le remplir
# copier deploy/taskflow.service vers /etc/systemd/system/
sudo systemctl daemon-reload && sudo systemctl enable taskflow
sudo ufw allow 8080
```

### À chaque mise à jour (depuis votre PC)
```bash
./build.sh && ./deploy/deploy.sh ubuntu@IP_DU_VPS
```
Logs : `sudo journalctl -u taskflow -f`

---

## 8. Interface (template Argon Dashboard)

L'interface reprend la charte du template **Argon Dashboard** de Creative Tim : sidenav blanche, en-tête dégradé indigo, cartes ombrées, tableaux « flush » et badges colorés. Elle est construite avec Bootstrap 5 et Bootstrap Icons (`frontend/src/styles.css`).
Pour utiliser les fichiers officiels du template (https://www.creative-tim.com/product/argon-dashboard-angular), copiez `argon-dashboard.css` et ses polices dans `frontend/src/assets/`, puis ajoutez-les dans `angular.json` → `styles`. Les classes utilisées (`card`, `table-flush`, `bg-gradient-primary`, `header`…) sont les mêmes.

---

## 9. Modèle de données

Voir `docs/diagramme-classes.puml` (s'ouvre avec l'extension PlantUML de VS Code).

```
User (id, email, password, tel, nom, prenom, photoContenu, photoTypeMime)  ── * ⟷ * ──  Role (id, libelle: ADMIN | COLLABORATEUR)
Client (id, nom, tel, email, adresse, logoContenu, logoTypeMime)  1 ── *  Projet (id, libelle, budget, duree, description)
Projet  1 ── *  Tache (id, titre, description, statut, priorite, dateEcheance)
User    1 ── *  Tache   (tâche affectée à un utilisateur, facultatif)
Fichier (id, nomOriginal, typeMime, taille, contenu, dateUpload, idUploadePar) ── *  rattache a 1 Tache OU 1 Projet (jamais les deux)
```
Suppressions : un client supprime ses projets (et leurs pièces jointes), un projet supprime ses tâches et ses documents, et un utilisateur supprimé libère ses tâches (elles deviennent non affectées).

### Portabilité MySQL / PostgreSQL (profil `render`)

Les colonnes binaires (`Fichier.contenu`, `Client.logoContenu`, `User.photoContenu`) sont mappées en `LONGBLOB` sur MySQL et en `bytea` sur PostgreSQL via `@JdbcTypeCode(SqlTypes.VARBINARY)` (évite le mécanisme "large object" OID de PostgreSQL, mal adapté ici). Pour déployer sur Render (base PostgreSQL, disque non persistant) :
```bash
docker build -t taskflow .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=render -e SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/DB -e DB_USER=... -e DB_PASSWORD=... taskflow
```
Le `Dockerfile` à la racine construit l'Angular puis le jar Spring Boot ; `backend/src/main/resources/application-render.properties` configure le driver PostgreSQL.
