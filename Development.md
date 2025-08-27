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
