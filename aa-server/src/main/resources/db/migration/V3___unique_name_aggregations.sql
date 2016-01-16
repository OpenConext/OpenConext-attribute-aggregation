--
-- Add traceability
--
ALTER TABLE aggregations ADD UNIQUE INDEX aggregations_name (name);
