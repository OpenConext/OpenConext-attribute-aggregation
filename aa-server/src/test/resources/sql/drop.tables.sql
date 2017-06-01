--
-- SELECT concat('DROP TABLE IF EXISTS ', table_name, ';') FROM information_schema.tables WHERE table_schema = 'aaserver';
--
DROP TABLE IF EXISTS aggregations_attributes;
DROP TABLE IF EXISTS aggregations_service_providers;
DROP TABLE IF EXISTS aggregations;
DROP TABLE IF EXISTS attributes;
DROP TABLE IF EXISTS schema_version;
DROP TABLE IF EXISTS service_providers;