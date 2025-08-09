# Fasting Service (Java 21, Spring Boot, Maven)

Ein kleiner **Fasten-Tracker Microservice** mit REST-API: `start`, `stop`, `status`, `history`.  
Persistenz: **PostgreSQL**. Enthält **Mock-Daten** und **Docker Compose** (App + DB).

---

## Voraussetzungen
- **Java 21** (Temurin/Eclipse Adoptium o.ä.)
- **Maven 3.9+**
- (optional) **Docker** & **Docker Compose** für Container-Start

---

## Projektstruktur
```
fasting-service/
 ├─ pom.xml
 ├─ Dockerfile
 ├─ docker-compose.yml
 ├─ src/main/java/com/larslab/fasting/
 │   ├─ FastingApplication.java
 │   ├─ controller/FastController.java
 │   ├─ service/FastService.java
 │   ├─ model/FastSession.java
 │   └─ repo/FastRepository.java
 └─ src/main/resources/
     ├─ application.yml
     ├─ application-dev.yml
     └─ application-docker.yml
```

---

## Schnellstart

### Variante A: Alles in Containern
```bash
docker compose up --build
```
- App: http://localhost:8080  
- DB:  postgres:16 (im Compose-Netzwerk `db:5432`)

**Stoppen:**
```bash
docker compose down
```

### Variante B: Lokal starten (DB via Docker)
```bash
# 1) Postgres lokal (Docker)
docker run --name fasting-db \
  -e POSTGRES_DB=fastingdb \
  -e POSTGRES_USER=fasting_user \
  -e POSTGRES_PASSWORD=fasting_pass \
  -p 5432:5432 -d postgres:16

# 2) App starten (Profil dev)
mvn spring-boot:run
```

---

## API

Basis-URL: `http://localhost:8080/api/fast`

### Swagger UI
Die vollständige API-Dokumentation ist über Swagger UI verfügbar:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

### REST Endpunkte

```bash
# Starten
curl -X POST http://localhost:8080/api/fast/start

# Status
curl http://localhost:8080/api/fast/status

# Stoppen
curl -X POST http://localhost:8080/api/fast/stop

# Historie
curl http://localhost:8080/api/fast/history
```

Health/Info (Actuator):
```
GET http://localhost:8080/actuator/health
GET http://localhost:8080/actuator/info
```

---

## Konfiguration & Profile

**Standardprofil:** `dev` (siehe `application.yml`)

- `application-dev.yml` (lokal):
  - `spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:fastingdb}`
  - `spring.datasource.username=${DB_USER:fasting_user}`
  - `spring.datasource.password=${DB_PASS:fasting_pass}`

- `application-docker.yml` (Container):
  - Verbindet auf `db:5432` (Compose-Service)

**Profil setzen (optional):**
```bash
# Lokal ein anderes Profil
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# oder als JAR
java -jar target/fasting-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

**Environment-Variablen (dev-Profil)**
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=fastingdb
DB_USER=fasting_user
DB_PASS=fasting_pass
```

---

## Build & Run ohne Docker

```bash
# Test & Build
mvn clean package

# Start aus dem JAR (Profil dev)
java -jar target/fasting-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

---

## Tests
```bash
mvn test
```

---

## Seed-Daten
Beim ersten Start legt die App **2 Sessions** an:
- 1 beendete Session (gestern, 18h)
- 1 aktive Session (läuft seit ~4h)

---

## Troubleshooting

- **Port 8080 belegt?**  
  Port freimachen oder App mit anderem Port starten:
  ```bash
  SERVER_PORT=8081 mvn spring-boot:run
  ```
  oder
  ```bash
  java -jar target/fasting-service-0.0.1-SNAPSHOT.jar --server.port=8081
  ```

- **DB-Verbindung schlägt fehl?**  
  Prüfen: läuft Postgres? Stimmt Benutzer/Passwort/DB-Name/Port?

- **Docker Compose startet App vor DB?**  
  Compose nutzt Healthcheck auf DB; App startet, sobald DB „healthy“ ist. Falls es hakt:
  ```bash
  docker compose down -v && docker compose up --build
  ```

---

## Nächste Schritte (optional)
- **Flyway** für DB-Migrationen
- **Auth** (z. B. Keycloak oder JWT)
- **DTOs & Validation** (saubere API-Verträge)
- **CI/CD** (GitHub Actions)

---
