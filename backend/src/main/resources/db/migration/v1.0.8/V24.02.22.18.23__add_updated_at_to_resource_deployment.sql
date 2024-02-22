ALTER TABLE resource_deployment
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

CREATE TRIGGER set_resource_deployment_timestamp
    BEFORE UPDATE ON resource_deployment
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();