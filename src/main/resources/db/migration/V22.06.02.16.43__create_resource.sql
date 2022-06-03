CREATE OR REPLACE FUNCTION trigger_set_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TABLE resource_type (
    type_id SERIAL PRIMARY KEY,
    resource_type VARCHAR( 8 ) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE resource (
    resource_id SERIAL PRIMARY KEY,
    url VARCHAR ( 512 ),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resource_type INT NOT NULL,
    FOREIGN KEY (resource_type)
        REFERENCES resource_type (type_id)
);

CREATE TABLE metric (
    metric_id SERIAL PRIMARY KEY,
    metric VARCHAR(256) NOT NULL,
    description VARCHAR (512),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE metric_value (
    metric_value_id SERIAL PRIMARY KEY,
    count BIGINT NOT NULL,
    value DECIMAL(20, 10) NOT NULL,
    resource_id INT NOT NULL,
    metric_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (resource_id)
        REFERENCES resource (resource_id),
    FOREIGN KEY (metric_id)
        REFERENCES metric (metric_id)
);

CREATE TRIGGER set_resource_timestamp
    BEFORE UPDATE ON resource
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_metric_value_timestamp
    BEFORE UPDATE ON metric_value
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
