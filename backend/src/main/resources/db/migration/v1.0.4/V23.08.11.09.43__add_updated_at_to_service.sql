ALTER TABLE service
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

CREATE TRIGGER set_service_timestamp
    BEFORE UPDATE ON service
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
