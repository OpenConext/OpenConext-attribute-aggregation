--
-- Moving from MySQLto the Galera cluster requires primary keys.
--
--
ALTER TABLE aggregations_attributes
  ADD PRIMARY KEY (aggregation_id, attribute_id);
ALTER TABLE aggregations_service_providers
  ADD PRIMARY KEY (aggregation_id, service_provider_id);