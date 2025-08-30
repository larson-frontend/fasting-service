# Database SQL Layout

This service uses Flyway for versioned schema migrations:

- Application migrations: `src/main/resources/db/migration/`
  - Files named `V{version}__{description}.sql` (e.g., `V1__baseline_core_schema.sql`)
- Manual/one-off scripts: `sql/manual/`
  - Ad-hoc or investigative SQL scripts that should not run automatically

Notes
- Prefer Flyway migrations for schema changes. Keep them idempotent where reasonable.
- Use manual scripts only for data fixes or exploratory tasks. Review carefully before running in production.
