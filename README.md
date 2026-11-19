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

**Erreurs** : réponse JSON `{ status, message, details }` avec les codes 400 (validation, avec un message par champ), 401 (non connecté / token invalide), 403 (droits insuffisants), 404 (introuvable) et 409 (email déjà utilisé).

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
User (id, email, password, tel, nom, prenom)  ── * ⟷ * ──  Role (id, libelle: ADMIN | COLLABORATEUR)
Client (id, nom, tel, email, adresse)  1 ── *  Projet (id, libelle, budget, duree, description)
Projet  1 ── *  Tache (id, titre, description, statut, priorite, dateEcheance)
User    1 ── *  Tache   (tâche affectée à un utilisateur, facultatif)
```
Suppressions : un client supprime ses projets, un projet supprime ses tâches, et un utilisateur supprimé libère ses tâches (elles deviennent non affectées).
