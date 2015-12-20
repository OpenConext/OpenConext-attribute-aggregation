INSERT INTO aggregations (id, name,user_identifier, user_display_name, ts)
VALUES
  (1, 'test aggregation', 'admin', 'John Doe', '2015-12-26 09:45:14');
INSERT INTO aggregations_attributes (aggregation_id, attribute_id)
VALUES
  (1, 1);
INSERT INTO aggregations_service_providers (aggregation_id, service_provider_id)
VALUES
  (1, 1);
INSERT INTO attributes (id, attribute_authority_id, name)
VALUES
  (1, 'aa1', 'urn:mace:dir:attribute-def:eduPersonOrcid');
INSERT INTO service_providers (id, entity_id)
VALUES
  (1, 'http://mock-sp');