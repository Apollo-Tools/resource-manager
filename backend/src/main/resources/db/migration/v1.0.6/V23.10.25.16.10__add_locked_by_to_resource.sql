ALTER TABLE resource
ADD COLUMN locked_by_deployment BIGINT DEFAULT NULL,
ADD CONSTRAINT locked_by_fkey FOREIGN KEY (locked_by_deployment) REFERENCES deployment(deployment_id);