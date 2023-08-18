CREATE TRIGGER set_function_timestamp
    BEFORE UPDATE ON function
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
