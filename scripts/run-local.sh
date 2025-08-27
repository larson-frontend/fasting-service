#!/usr/bin/env bash
# Local dev runner: builds the JAR, builds the Docker image, ensures a local Postgres, then runs the service.
# Idempotent: you can re-run; container/image names reused.
set -euo pipefail

APP_NAME="fasting-service"
DB_CONTAINER="fasting-db"
DB_IMAGE="postgres:16-alpine"
DB_PORT="5432"
APP_PORT="10000"
JAR_SKIP_TESTS="-DskipTests"
PROFILE="prod"

log() { printf "\n[run-local] %s\n" "$*"; }

# 1. Start (or reuse) local Postgres
if ! docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
  if docker ps -a --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
    log "Removing old stopped DB container"; docker rm -f "${DB_CONTAINER}" >/dev/null
  fi
  log "Starting Postgres container (${DB_IMAGE}) on port ${DB_PORT}";
  docker run -d --name "${DB_CONTAINER}" -e POSTGRES_PASSWORD=postgres -p ${DB_PORT}:5432 ${DB_IMAGE} >/dev/null
else
  log "Postgres already running (container: ${DB_CONTAINER})"
fi

log "Waiting for Postgres to accept connections..."
for i in {1..15}; do
  if docker exec "${DB_CONTAINER}" pg_isready -U postgres >/dev/null 2>&1; then
    break
  fi
  sleep 1
  if [ "$i" -eq 15 ]; then
    echo "Postgres not ready after 15s" >&2; exit 1
  fi
done

# 2. Build application (skip tests for faster iteration)
log "Building JAR"
mvn -q ${JAR_SKIP_TESTS} package

# 3. Build Docker image
log "Building Docker image ${APP_NAME}"
docker build -q -t "${APP_NAME}" .

# 4. Stop previous running app container if exists
if docker ps --format '{{.Names}}' | grep -q "^${APP_NAME}$"; then
  log "Stopping existing app container"
  docker stop "${APP_NAME}" >/dev/null || true
fi

# 5. Run application
log "Starting application on port ${APP_PORT} (profile=${PROFILE})"
docker run --rm --name "${APP_NAME}" \
  -p ${APP_PORT}:${APP_PORT} \
  -e PORT=${APP_PORT} \
  -e SPRING_PROFILES_ACTIVE=${PROFILE} \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=${DB_PORT} \
  -e DB_NAME=postgres \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=dev-local-secret \
  -e CORS_ALLOWED_ORIGINS=http://localhost:5173 \
  ${APP_NAME} &
APP_PID=$!

# 6. Wait for health endpoint
log "Waiting for health endpoint..."
ATTEMPTS=30
for i in $(seq 1 ${ATTEMPTS}); do
  if curl -fsS "http://localhost:${APP_PORT}/actuator/health" >/dev/null 2>&1; then
    log "Service is UP: http://localhost:${APP_PORT}"; break
  fi
  sleep 1
  [ "$i" -eq ${ATTEMPTS} ] && { echo "Service failed to start" >&2; exit 2; }
done

log "Tail logs (CTRL+C to stop). Container will be removed on exit."
docker logs -f "${APP_NAME}" || true
