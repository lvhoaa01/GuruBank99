-- One-shot privilege fix.
-- Run once as the postgres superuser if schema.sql was loaded before the
-- GRANT statements were added to schema.sql.
--
--   psql -h 127.0.0.1 -U postgres -d bank99 -f db\grant-permissions.sql
--
-- This does NOT drop or modify any data — only grants privileges.

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES    IN SCHEMA public TO bank99user;
GRANT USAGE, SELECT, UPDATE              ON ALL SEQUENCES IN SCHEMA public TO bank99user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES    TO bank99user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT, UPDATE              ON SEQUENCES TO bank99user;

\echo 'Permissions granted to bank99user.'
