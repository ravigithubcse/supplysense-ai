-- SupplySense AI - Database Initialization Script
-- Creates all schemas for microservices

-- Auth Schema
CREATE SCHEMA IF NOT EXISTS auth;

-- Supply Chain Schema
CREATE SCHEMA IF NOT EXISTS supply_chain;

-- Risk Schema
CREATE SCHEMA IF NOT EXISTS risk;

-- Events Schema
CREATE SCHEMA IF NOT EXISTS events;

-- Notifications Schema
CREATE SCHEMA IF NOT EXISTS notifications;

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "timescaledb" CASCADE;
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA auth TO supplysense;
GRANT ALL PRIVILEGES ON SCHEMA supply_chain TO supplysense;
GRANT ALL PRIVILEGES ON SCHEMA risk TO supplysense;
GRANT ALL PRIVILEGES ON SCHEMA events TO supplysense;
GRANT ALL PRIVILEGES ON SCHEMA notifications TO supplysense;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA auth TO supplysense;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA supply_chain TO supplysense;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA risk TO supplysense;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA events TO supplysense;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA notifications TO supplysense;

ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT ALL ON TABLES TO supplysense;
ALTER DEFAULT PRIVILEGES IN SCHEMA supply_chain GRANT ALL ON TABLES TO supplysense;
ALTER DEFAULT PRIVILEGES IN SCHEMA risk GRANT ALL ON TABLES TO supplysense;
ALTER DEFAULT PRIVILEGES IN SCHEMA events GRANT ALL ON TABLES TO supplysense;
ALTER DEFAULT PRIVILEGES IN SCHEMA notifications GRANT ALL ON TABLES TO supplysense;

\echo 'SupplySense AI schemas created successfully'
