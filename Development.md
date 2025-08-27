# Fasting App – Development Guide (Render + Spring Boot + Postgres)

## 1. Project Overview
Full-stack fasting tracker: Frontend Vue 3 + TS, Backend Spring Boot 3.3.2 (JWT), PostgreSQL, Deployment via Render (Docker). Ziel: sofort klonbar, lokal testbar, 1-Klick-Deploy auf Render.

## 2. What Changed
- Korrektes Docker Multi-Stage Build + JVM-OPTS
- Render `render.yaml` mit Healthcheck und DB-ENV-Wiring (Host/Port/DB/User/Pass)
- JDBC-URL korrekt (`jdbc:postgresql://…` + `sslmode=require`)
- Free-Tier-Hinweise: Webservice schläft nach Inaktivität; 750 h/Monat; Free-DB ohne Backups, 30-Tage-Limit
- Hikari-Pool reduziert (Free-Tier-tauglich)
- Actuator Health `/actuator/health`
- Controller-Snippets korrigiert (vollständige Methoden)
- DB-Schema: `timestamptz`, Unique-Index für aktive Session, Zeit-Constraint

## 3. Backend: Controller & Security
```java
@RestController
@RequestMapping("/api/fast")
@PreAuthorize("hasRole('USER')")
public class FastController {

  @GetMapping("/user/{identifier}/status")
  public ResponseEntity<FastingStatusResponse> status(@PathVariable String identifier, Authentication auth) { /* ... */ }

  @GetMapping("/user/{identifier}/history")
  public ResponseEntity<List<FastingSessionDto>> history(@PathVariable String identifier, Authentication auth) { /* ... */ }

  @PostMapping("/user/{identifier}/start")
  public ResponseEntity<Void> start(@PathVariable String identifier, @RequestBody StartRequest req, Authentication auth) { /* ... */ }

  @PostMapping("/user/{identifier}/stop")
  public ResponseEntity<Void> stop(@PathVariable String identifier, Authentication auth) { /* ... */ }
}
```

## 4. Deploy to Render (Steps)

1. Repository vorbereiten: Sicherstellen, dass `render.yaml` im Repository-Wurzelverzeichnis liegt und Dockerfile funktioniert (lokal `docker build .`).
2. **Connect GitHub with least privilege:** In Render beim Verbinden mit GitHub "Only select repositories" wählen und ausschließlich das Repo `fasting-service` freigeben (Backend). `fasting-frontend` NICHT auswählen – das wird nur benötigt, wenn das Frontend später ebenfalls auf Render (als *Static Site*) deployt wird.
3. New Web Service anlegen: "+ New" → Web Service → Repository `fasting-service` auswählen.
4. Render erkennt automatisch `render.yaml` → bestätigen.
5. Environment: Docker (aus `render.yaml`), Region: Frankfurt (wie definiert), Plan: Free (oder höher falls nötig).
6. Datenbank-Provisionierung: Render legt anhand `databases`-Block die Postgres-Instanz an; env vars werden injiziert (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD).
7. Health Check prüfen: Nach erstem Deploy sollte `/actuator/health` Status `UP` liefern.
8. Logs prüfen (Startup: Flyway Migrations, Hibernate, Security Filter Chain), keine Stacktraces.
9. Manuelles Smoke-Testing: `GET /actuator/health`, ggf. vorhandene API-Endpunkte.
10. AutoDeploy aktiviert lassen für main/develop – Feature-Branches nach Bedarf.

## 5. Production Notes

- Free Tier Schlafmodus: Service schläft nach Idle; erster Request kalt.
- DB Backups: Free Postgres hat keine automatischen Backups – ggf. extern sichern.
- Pooling: Konservative Hikari-Settings empfohlen (z.B. maxPoolSize 5–10 im Free Tier).
- Security: JWT Secret als Secret Env Var hinterlegen (nicht den CI-Test-Secret verwenden).
- Observability: Actuator Health ok; optional weitere Endpoints absichern / deaktivieren.
- Upgrades: Regelmäßig `mvn versions:display-dependency-updates` prüfen.
- Image Scan: CI nutzt Trivy (HIGH/CRITICAL Gate). Bei False Positives `.trivyignore` mit CVE notieren.
- **GitHub permissions:** Least privilege – Render hat nur Zugriff auf `fasting-service`. `fasting-frontend` erst freigeben, wenn es als Static Site deployed wird.
