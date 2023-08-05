ALTER TABLE metric_value
    DROP CONSTRAINT metric_value_resource_id_fkey,
    ADD FOREIGN KEY (resource_id) REFERENCES resource(resource_id) ON DELETE CASCADE;
