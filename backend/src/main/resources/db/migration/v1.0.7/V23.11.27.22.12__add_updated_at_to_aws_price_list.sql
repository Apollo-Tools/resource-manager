ALTER TABLE aws_price
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

CREATE TRIGGER set_aws_price_timestamp
    BEFORE UPDATE ON aws_price
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();