ALTER TABLE resource_type
    ADD CONSTRAINT unique_resource_type UNIQUE (resource_type);

ALTER TABLE resource
    ADD CONSTRAINT unique_resource_url UNIQUE (url);

ALTER TABLE metric
    ADD CONSTRAINT unique_metric UNIQUE (metric);

ALTER TABLE metric_value
    ADD CONSTRAINT unique_metric_value_mapping UNIQUE (metric_id, resource_id);
