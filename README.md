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

---

## 🚀 API Endpoints für Copilot/Entwickler

### 1. **Fasten-Session starten**
```http
POST /api/fast/start
Content-Type: application/json
```

**Beschreibung:** Startet eine neue Fasten-Session oder gibt die bereits aktive Session zurück.

**cURL Beispiel:**
```bash
curl -X POST http://localhost:8080/api/fast/start \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "id": 1,
  "startAt": "2025-08-09T10:30:00Z",
  "endAt": null,
  "duration": "PT4H30M"
}
```

---

### 2. **Fasten-Session beenden**
```http
POST /api/fast/stop
Content-Type: application/json
```

**Beschreibung:** Beendet die aktuell aktive Fasten-Session.

**cURL Beispiel:**
```bash
curl -X POST http://localhost:8080/api/fast/stop \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "id": 1,
  "startAt": "2025-08-09T10:30:00Z",
  "endAt": "2025-08-09T18:30:00Z",
  "duration": "PT8H"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-08-09T18:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Kein aktives Fasten",
  "path": "/api/fast/stop"
}
```

---

### 3. **Status der aktuellen Fasten-Session**
```http
GET /api/fast/status
```

**Beschreibung:** Gibt den Status der aktuellen Fasten-Session zurück (aktiv/inaktiv mit Dauer).

**cURL Beispiel:**
```bash
curl -X GET http://localhost:8080/api/fast/status
```

**Response (aktive Session):**
```json
{
  "active": true,
  "hours": 4,
  "minutes": 30,
  "since": "2025-08-09T10:30:00Z"
}
```

**Response (keine aktive Session):**
```json
{
  "active": false
}
```

---

### 4. **Historie aller Fasten-Sessions**
```http
GET /api/fast/history
```

**Beschreibung:** Gibt eine Liste aller bisherigen Fasten-Sessions zurück.

**cURL Beispiel:**
```bash
curl -X GET http://localhost:8080/api/fast/history
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "startAt": "2025-08-08T20:00:00Z",
    "endAt": "2025-08-09T14:00:00Z",
    "duration": "PT18H"
  },
  {
    "id": 2,
    "startAt": "2025-08-09T10:30:00Z",
    "endAt": null,
    "duration": "PT4H30M"
  }
]
```

---

### 5. **Health Check (Actuator)**
```http
GET /actuator/health
```

**Beschreibung:** Überprüft den Gesundheitsstatus der Anwendung.

**cURL Beispiel:**
```bash
curl -X GET http://localhost:8080/actuator/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

---

### 6. **Application Info (Actuator)**
```http
GET /actuator/info
```

**Beschreibung:** Gibt Informationen über die Anwendung zurück.

**cURL Beispiel:**
```bash
curl -X GET http://localhost:8080/actuator/info
```

---

## 🔧 Datenmodell

### FastSession Entity
```json
{
  "id": "Long - Eindeutige ID der Fasten-Session",
  "startAt": "Instant - Startzeitpunkt der Fasten-Session (ISO 8601)",
  "endAt": "Instant - Endzeitpunkt (null wenn noch aktiv, ISO 8601)",
  "duration": "Duration - Berechnete Dauer (ISO 8601 Format)"
}
```

**Beispiel-Werte:**
- `startAt`: `"2025-08-09T10:30:00Z"`
- `endAt`: `"2025-08-09T18:30:00Z"` oder `null`
- `duration`: `"PT8H"` (8 Stunden) oder `"PT4H30M"` (4 Stunden 30 Minuten)

---

## 🐳 Docker Integration

**Vollständiger Stack starten:**
```bash
docker compose up --build
```

**Services:**
- **App:** `http://localhost:8080`
- **PostgreSQL:** `localhost:5432`
- **Database:** `fastingdb`
- **User:** `fasting_user` / `fasting_pass`

---

## 📝 Entwickler-Notizen für Copilot

### Wichtige Endpunkte für Integration:
1. **`POST /api/fast/start`** - Session starten
2. **`POST /api/fast/stop`** - Session beenden  
3. **`GET /api/fast/status`** - Aktueller Status
4. **`GET /api/fast/history`** - Alle Sessions

### Error Handling:
- `400 Bad Request` - Wenn keine aktive Session zum Stoppen vorhanden ist
- `500 Internal Server Error` - Bei Datenbankfehlern
- Alle Responses sind im JSON-Format

### Headers:
- `Content-Type: application/json` für POST-Requests
- Keine Authentifizierung erforderlich (für Demo-Zwecke)

### Swagger/OpenAPI:
- Vollständige API-Dokumentation: `http://localhost:8080/swagger-ui.html`
- OpenAPI 3.0 Spec: `http://localhost:8080/api-docs`

---

## 🎨 Frontend Integration Guide

### CORS Konfiguration ✅
Das Backend ist vollständig für Frontend-Entwicklung konfiguriert:
- **Unterstützte Ports:** 3000 (Vue/React), 5173 (Vite), 8080 (Vue CLI), 4200 (Angular), 8000
- **Methoden:** GET, POST, PUT, DELETE, OPTIONS
- **Headers:** Alle erlaubt
- **Credentials:** Unterstützt

### ⚠️ Wichtige Hinweise für Frontend-Entwicklung

**Keine Dummy-Daten mehr!**
- Die App startet jetzt mit einer **leeren Datenbank**
- Keine automatisch aktiven Sessions
- Frontend kann sauber mit leerem Zustand starten

**Erste Schritte:**
1. Backend starten: `docker compose up -d`
2. Status prüfen: `GET /api/fast/status` → `{"active": false}`
3. Session starten: `POST /api/fast/start`
4. Status prüfen: `GET /api/fast/status` → `{"active": true, ...}`

**Datenbank zurücksetzen (bei Problemen):**
```bash
docker compose down -v && docker compose up -d
```

### Vue.js/TypeScript Interfaces

```typescript
// TypeScript Interfaces für Vue.js Frontend-Integration
interface FastSession {
  id: number;
  startAt: string; // ISO 8601 DateTime
  endAt: string | null; // ISO 8601 DateTime oder null
  duration: string; // ISO 8601 Duration (z.B. "PT8H30M")
}

interface FastStatus {
  active: boolean;
  hours?: number;
  minutes?: number;
  since?: string; // ISO 8601 DateTime
}

interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

// Vue 3 Composition API Types
interface FastingState {
  currentSession: Ref<FastSession | null>;
  status: Ref<FastStatus | null>;
  history: Ref<FastSession[]>;
  isLoading: Ref<boolean>;
  error: Ref<string | null>;
}
```

### Vue.js API Service Beispiel

```typescript
// Vue.js API Service mit Axios oder Fetch
import { ref, reactive } from 'vue';

class FastingApiService {
  private baseUrl = 'http://localhost:8080/api/fast';

  async startSession(): Promise<FastSession> {
    const response = await fetch(`${this.baseUrl}/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to start session');
    return response.json();
  }

  async stopSession(): Promise<FastSession> {
    const response = await fetch(`${this.baseUrl}/stop`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) {
      if (response.status === 400) {
        throw new Error('Keine aktive Fasten-Session vorhanden');
      }
      throw new Error('Failed to stop session');
    }
    return response.json();
  }

  async getStatus(): Promise<FastStatus> {
    const response = await fetch(`${this.baseUrl}/status`);
    if (!response.ok) throw new Error('Failed to get status');
    return response.json();
  }

  async getHistory(): Promise<FastSession[]> {
    const response = await fetch(`${this.baseUrl}/history`);
    if (!response.ok) throw new Error('Failed to get history');
    return response.json();
  }

  async checkHealth(): Promise<any> {
    const response = await fetch('http://localhost:8080/actuator/health');
    if (!response.ok) throw new Error('Service unhealthy');
    return response.json();
  }
}

// Vue 3 Global Service Registration
// main.ts
import { createApp } from 'vue';
import App from './App.vue';

const app = createApp(App);
app.provide('fastingApi', new FastingApiService());
app.mount('#app');
```

### Vue 3 Composition API Composable

```typescript
// composables/useFastingService.ts
import { ref, computed, onMounted, onUnmounted, inject } from 'vue';

export const useFastingService = () => {
  const fastingApi = inject<FastingApiService>('fastingApi');
  if (!fastingApi) throw new Error('FastingApiService not provided');

  // Reactive state
  const currentSession = ref<FastSession | null>(null);
  const status = ref<FastStatus | null>(null);
  const history = ref<FastSession[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  // Computed properties
  const isActive = computed(() => status.value?.active || false);
  const currentDuration = computed(() => {
    if (!status.value?.active) return null;
    return `${status.value.hours || 0}h ${status.value.minutes || 0}m`;
  });

  // Methods
  const refreshStatus = async () => {
    try {
      isLoading.value = true;
      error.value = null;
      const statusData = await fastingApi.getStatus();
      status.value = statusData;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error';
    } finally {
      isLoading.value = false;
    }
  };

  const refreshHistory = async () => {
    try {
      isLoading.value = true;
      error.value = null;
      const historyData = await fastingApi.getHistory();
      history.value = historyData;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error';
    } finally {
      isLoading.value = false;
    }
  };

  const startSession = async () => {
    try {
      isLoading.value = true;
      error.value = null;
      const session = await fastingApi.startSession();
      currentSession.value = session;
      await refreshStatus();
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to start session';
    } finally {
      isLoading.value = false;
    }
  };

  const stopSession = async () => {
    try {
      isLoading.value = true;
      error.value = null;
      const session = await fastingApi.stopSession();
      currentSession.value = session;
      await refreshStatus();
      await refreshHistory();
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to stop session';
    } finally {
      isLoading.value = false;
    }
  };

  // Auto-refresh interval
  let refreshInterval: number | null = null;

  const startAutoRefresh = () => {
    if (status.value?.active && !refreshInterval) {
      refreshInterval = setInterval(refreshStatus, 30000);
    }
  };

  const stopAutoRefresh = () => {
    if (refreshInterval) {
      clearInterval(refreshInterval);
      refreshInterval = null;
    }
  };

  // Lifecycle hooks
  onMounted(async () => {
    await refreshStatus();
    await refreshHistory();
    if (status.value?.active) {
      startAutoRefresh();
    }
  });

  onUnmounted(() => {
    stopAutoRefresh();
  });

  // Watch for status changes to manage auto-refresh
  watch(
    () => status.value?.active,
    (isActive) => {
      if (isActive) {
        startAutoRefresh();
      } else {
        stopAutoRefresh();
      }
    }
  );

  return {
    // State
    currentSession: readonly(currentSession),
    status: readonly(status),
    history: readonly(history),
    isLoading: readonly(isLoading),
    error: readonly(error),
    
    // Computed
    isActive,
    currentDuration,
    
    // Methods
    startSession,
    stopSession,
    refreshStatus,
    refreshHistory
  };
};
```

### Vue Component Beispiele

```vue
<!-- FastingTimer.vue - Hauptkomponente -->
<template>
  <div class="fasting-timer">
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div class="status-display">
      <h2 v-if="isActive" class="active-status">
        Fasten läuft: {{ currentDuration }}
      </h2>
      <h2 v-else class="inactive-status">
        Kein aktives Fasten
      </h2>
    </div>

    <div class="controls">
      <button 
        v-if="!isActive"
        @click="startSession"
        :disabled="isLoading"
        class="start-button"
      >
        {{ isLoading ? 'Starte...' : 'Fasten starten' }}
      </button>
      
      <button 
        v-else
        @click="stopSession"
        :disabled="isLoading"
        class="stop-button"
      >
        {{ isLoading ? 'Stoppe...' : 'Fasten beenden' }}
      </button>
    </div>

    <div v-if="status?.active" class="live-timer">
      <p>Seit: {{ formatDateTime(status.since!) }}</p>
      <div class="progress-circle">
        <!-- Live Timer Implementation -->
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useFastingService } from '@/composables/useFastingService';
import { formatDateTime } from '@/utils/dateUtils';

const {
  status,
  isActive,
  currentDuration,
  isLoading,
  error,
  startSession,
  stopSession
} = useFastingService();
</script>

<style scoped>
.fasting-timer {
  text-align: center;
  padding: 2rem;
}

.start-button {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1.2rem;
  cursor: pointer;
  transition: background 0.3s;
}

.start-button:hover {
  background: #45a049;
}

.stop-button {
  background: #f44336;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1.2rem;
  cursor: pointer;
  transition: background 0.3s;
}

.stop-button:hover {
  background: #da190b;
}

.error-message {
  color: #f44336;
  margin-bottom: 1rem;
  padding: 0.5rem;
  border: 1px solid #f44336;
  border-radius: 4px;
  background: #ffebee;
}

.active-status {
  color: #4CAF50;
}

.inactive-status {
  color: #666;
}
</style>
```

```vue
<!-- FastingHistory.vue - History Komponente -->
<template>
  <div class="fasting-history">
    <h3>Fasten-Historie</h3>
    
    <div v-if="history.length === 0" class="no-history">
      Noch keine Fasten-Sessions vorhanden.
    </div>
    
    <div v-else class="history-list">
      <div 
        v-for="session in history" 
        :key="session.id"
        class="history-item"
        :class="{ 'active-session': !session.endAt }"
      >
        <div class="session-info">
          <div class="session-date">
            {{ formatDateTime(session.startAt) }}
          </div>
          <div class="session-duration">
            {{ formatDuration(session.duration) }}
          </div>
          <div v-if="session.endAt" class="session-end">
            bis {{ formatDateTime(session.endAt) }}
          </div>
          <div v-else class="session-active">
            Läuft noch...
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useFastingService } from '@/composables/useFastingService';
import { formatDateTime, formatDuration } from '@/utils/dateUtils';

const { history } = useFastingService();
</script>

<style scoped>
.history-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 0.5rem;
  background: white;
}

.active-session {
  border-color: #4CAF50;
  background: #f1f8e9;
}

.session-active {
  color: #4CAF50;
  font-weight: bold;
}

.no-history {
  text-align: center;
  color: #666;
  font-style: italic;
  padding: 2rem;
}
</style>
```

### Vue.js Utility Functions

```typescript
// utils/dateUtils.ts
export const formatDuration = (duration: string): string => {
  // Konvertiert "PT8H30M" zu "8h 30m"
  const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
  if (!match) return '0m';
  
  const hours = match[1] ? `${match[1]}h` : '';
  const minutes = match[2] ? `${match[2]}m` : '';
  return `${hours} ${minutes}`.trim() || '0m';
};

export const formatDateTime = (isoString: string): string => {
  return new Date(isoString).toLocaleString('de-DE', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

export const calculateElapsedTime = (startTime: string): { hours: number; minutes: number } => {
  const start = new Date(startTime);
  const now = new Date();
  const diffMs = now.getTime() - start.getTime();
  
  const hours = Math.floor(diffMs / (1000 * 60 * 60));
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
  
  return { hours, minutes };
};

// stores/fastingStore.ts - Pinia Store (optional)
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useFastingStore = defineStore('fasting', () => {
  const currentSession = ref<FastSession | null>(null);
  const status = ref<FastStatus | null>(null);
  const history = ref<FastSession[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  const isActive = computed(() => status.value?.active || false);
  const currentDuration = computed(() => {
    if (!status.value?.active) return null;
    return `${status.value.hours || 0}h ${status.value.minutes || 0}m`;
  });

  return {
    currentSession,
    status,
    history,
    isLoading,
    error,
    isActive,
    currentDuration
  };
});
```

### Vue.js Feature Suggestions

**Kernfunktionen:**
1. **Start/Stop Button** - Große, zentrale Schaltfläche mit Vue Transitions
2. **Live Timer** - Zeigt aktuelle Fastendauer mit reactive updates
3. **Session History** - Liste vergangener Sessions mit Vue Transition Groups
4. **Status Dashboard** - Übersicht über aktuelle Session

**Vue-spezifische Features:**
1. **Composition API** - Saubere Trennung von Business Logic
2. **Pinia Store** - Zentrales State Management (optional)
3. **Vue Router** - Navigation zwischen Timer und History
4. **Teleport** - Modals und Notifications
5. **Suspense** - Async Component Loading

**Erweiterte Features:**
1. **Progress Visualization** - SVG Kreisdiagramm mit Vue Transitions
2. **Statistics** - Computed Properties für Durchschnittswerte
3. **Notifications** - Browser-API mit Vue Composables
4. **Goal Setting** - Reactive Formulare mit Validation
5. **Export Function** - History als CSV/JSON mit File API

**Vue.js Ecosystem Integration:**
- **Vite** - Schneller Build mit HMR
- **Vue DevTools** - Debugging Support
- **Vitest** - Testing Framework
- **Nuxt** - SSR/SSG (optional für SEO)

**Responsive Design mit Vue:**
- **CSS Modules** oder **Scoped Styles**
- **Vue Use** - Utility Composables (useBreakpoints, useSwipe)
- **Transition Components** - Smooth Animations
- **Dynamic Components** - Mobile vs Desktop Views

### Environment Variables für Vue.js

```bash
# .env für Vue.js Frontend
VITE_API_BASE_URL=http://localhost:8080/api/fast
VITE_HEALTH_CHECK_URL=http://localhost:8080/actuator/health
VITE_REFRESH_INTERVAL=30000
VITE_APP_TITLE=Fasting Tracker
```

```typescript
// vite-env.d.ts
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
  readonly VITE_HEALTH_CHECK_URL: string;
  readonly VITE_REFRESH_INTERVAL: string;
  readonly VITE_APP_TITLE: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
```

### Testing Scenarios für Vue.js

```typescript
// Vue Test Utils + Vitest
import { mount } from '@vue/test-utils';
import { describe, it, expect, vi } from 'vitest';
import FastingTimer from '@/components/FastingTimer.vue';

describe('FastingTimer', () => {
  it('shows start button when not active', () => {
    const wrapper = mount(FastingTimer, {
      global: {
        provide: {
          fastingApi: mockFastingService
        }
      }
    });
    
    expect(wrapper.find('.start-button').text()).toBe('Fasten starten');
  });

  it('shows stop button when active', async () => {
    const wrapper = mount(FastingTimer, {
      global: {
        provide: {
          fastingApi: mockActiveFastingService
        }
      }
    });
    
    expect(wrapper.find('.stop-button').text()).toBe('Fasten beenden');
  });
});

// E2E Tests mit Cypress
const testScenarios = {
  'Happy Path': {
    1: 'User navigates to app',
    2: 'Clicks "Fasten starten" button',
    3: 'Timer starts and shows active status',
    4: 'User clicks "Fasten beenden"',
    5: 'Session appears in history'
  },
  'Error Handling': {
    1: 'Try to stop when no active session',
    2: 'Handle network errors gracefully',
    3: 'Show loading states during API calls',
    4: 'Validate form inputs'
  },
  'Vue-specific Tests': {
    1: 'Reactive data updates correctly',
    2: 'Computed properties recalculate',
    3: 'Component lifecycle works',
    4: 'Event handling functions properly'
  }
};
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
