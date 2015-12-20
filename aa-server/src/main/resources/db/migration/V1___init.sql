--
-- Initial tables
--
CREATE TABLE aggregations (
  id   MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;

CREATE TABLE attributes (
  id                     MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  attribute_authority_id VARCHAR(255) NOT NULL,
  name                   VARCHAR(255) NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;

ALTER TABLE attributes ADD UNIQUE INDEX attributes_attribute_authority_id_name (attribute_authority_id, name);

CREATE TABLE service_providers (
  id        MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  entity_id VARCHAR(255) NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;

ALTER TABLE service_providers ADD UNIQUE INDEX service_providers_entity_id (entity_id);

CREATE TABLE aggregations_service_providers (
  aggregation_id      MEDIUMINT NOT NULL,
  service_provider_id MEDIUMINT NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;

ALTER TABLE aggregations_service_providers ADD INDEX aggregations_service_providers_aggregation_id (aggregation_id);
ALTER TABLE aggregations_service_providers ADD INDEX aggregations_service_providers_service_provider_id (service_provider_id);

CREATE TABLE aggregations_attributes (
  aggregation_id MEDIUMINT NOT NULL,
  attribute_id   MEDIUMINT NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;

ALTER TABLE aggregations_attributes ADD INDEX aggregations_attributes_aggregation_id (aggregation_id);
ALTER TABLE aggregations_attributes ADD INDEX aggregations_attributes_attribute_id (attribute_id);
