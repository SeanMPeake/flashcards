-- Database setup: one schema and one login per service.
-- Each login owns its own schema and has no rights on any other.
-- Postgres runs this file only when the database is first created.

-- User service
CREATE ROLE user_svc LOGIN PASSWORD 'user_svc';   -- local development only
CREATE SCHEMA users AUTHORIZATION user_svc;
