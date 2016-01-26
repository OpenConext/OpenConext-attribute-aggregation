--
-- We can at this stage of development
--
DELETE FROM aggregations;
DELETE FROM aggregations_attributes;
DELETE FROM aggregations_service_providers;
DELETE FROM attributes;
DELETE FROM service_providers;

--
-- Because an attribute can be marked as skip_consent we can't re-use attributes over aggregations
--
ALTER TABLE attributes DROP INDEX attributes_attribute_authority_id_name;
ALTER TABLE attributes ADD skip_consent tinyint(1) DEFAULT 0;
