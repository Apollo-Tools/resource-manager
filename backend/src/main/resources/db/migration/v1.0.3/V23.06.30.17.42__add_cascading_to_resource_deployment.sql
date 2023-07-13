ALTER TABLE resource_deployment
    DROP CONSTRAINT resource_deployment_function_id_fkey,
    ADD FOREIGN KEY (function_id) REFERENCES function (function_id) ON DELETE CASCADE,
    DROP CONSTRAINT resource_deployment_service_id_fkey,
    ADD FOREIGN KEY (service_id) REFERENCES service (service_id) ON DELETE CASCADE,
    DROP CONSTRAINT resource_deployment_resource_id_fkey,
    ADD FOREIGN KEY (resource_id) REFERENCES resource (resource_id) ON DELETE CASCADE;
