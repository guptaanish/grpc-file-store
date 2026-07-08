# Database Migration

Procedure for safely creating, reviewing, and applying database schema changes using migration tools.

## Steps

1. **Assess the change** — Determine what schema modification is needed (new table, column addition, index, constraint change, data migration) and its impact on existing data.

2. **Choose migration strategy** — Based on the project setup:
   - **Flyway** — Versioned SQL migrations (`V1__description.sql`)
   - **Liquibase** — XML/YAML/SQL changelogs
   - If no migration tool exists, recommend and set up Flyway (preferred for SQL-first projects) or Liquibase (preferred for multi-database support)

3. **Write the migration script** — Create the migration file following conventions:
   - Use sequential versioning (`V1`, `V2`, etc. for Flyway)
   - Write both UP and DOWN (rollback) logic where possible
   - Use idempotent statements (`IF NOT EXISTS`, `IF EXISTS`) when supported
   - Separate DDL (schema) from DML (data) migrations
   - Keep migrations small and focused on one logical change

4. **Review for backward compatibility** — Ensure the migration is safe for zero-downtime deployments:
   - Adding columns: use nullable or provide defaults
   - Removing columns: deploy code change first, then drop column in a later release
   - Renaming: add new → migrate data → update code → drop old
   - Adding indexes: use `CONCURRENTLY` where supported to avoid table locks

5. **Test the migration** — Verify:
   - Migration applies cleanly on an empty database
   - Migration applies cleanly on a database with existing data
   - Rollback works correctly (if applicable)
   - Application starts and passes tests after migration
   - No data loss or corruption

6. **Update JPA entities and repositories** — Ensure entity classes match the new schema. Update queries, indexes, and constraints in code.

7. **Run full test suite** — Execute unit and integration tests to confirm nothing is broken by the schema change.

8. **Document the change** — Update relevant documentation, noting any required deployment order or manual steps.
