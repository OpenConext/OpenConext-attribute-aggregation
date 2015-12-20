--
-- Add traceability
--
ALTER TABLE aggregations ADD user_identifier VARCHAR(256) NOT NULL;
ALTER TABLE aggregations ADD user_display_name VARCHAR(256) NOT NULL;
ALTER TABLE aggregations ADD ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
