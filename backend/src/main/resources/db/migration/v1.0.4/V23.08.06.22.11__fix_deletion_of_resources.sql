ALTER TABLE resource
    DROP CONSTRAINT resource_subresource_id_fkey,
    ADD FOREIGN KEY (subresource_id) REFERENCES resource(resource_id) ON DELETE CASCADE;
