ALTER TABLE metric_value
    RENAME COLUMN value TO value_number;
ALTER TABLE metric_value
    ALTER COLUMN value_number DROP DEFAULT;
ALTER TABLE metric_value
    ALTER COLUMN count DROP DEFAULT;
ALTER TABLE metric_value
    ALTER COLUMN value_number DROP NOT NULL;
ALTER TABLE metric_value
    ALTER COLUMN count DROP NOT NULL;

ALTER TABLE metric_value
    ADD COLUMN value_string VARCHAR(1024),
    ADD COLUMN value_bool BOOLEAN;

CREATE TABLE metric_type (
    metric_type_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type VARCHAR(256) NOT NULL
);

ALTER TABLE metric
    ADD COLUMN metric_type_id BIGINT,
    ADD CONSTRAINT metric_type_id_fkey FOREIGN KEY (metric_type_id) REFERENCES metric_type(metric_type_id);
