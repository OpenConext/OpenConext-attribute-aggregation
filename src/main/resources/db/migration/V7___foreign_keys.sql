ALTER TABLE aggregations_attributes
  ADD CONSTRAINT fk_aggregations_attributes_aggregation_id FOREIGN KEY (aggregation_id) REFERENCES aggregations (id);
ALTER TABLE aggregations_attributes
  ADD CONSTRAINT fk_aggregations_attributes_attribute_id FOREIGN KEY (attribute_id) REFERENCES attributes (id);
ALTER TABLE aggregations_service_providers
  ADD CONSTRAINT fk_aggregations_service_providers_aggregation_id FOREIGN KEY (aggregation_id) REFERENCES aggregations (id);
ALTER TABLE aggregations_service_providers
  ADD CONSTRAINT fk_aggregations_service_providers_service_provider_id FOREIGN KEY (service_provider_id) REFERENCES service_providers (id);