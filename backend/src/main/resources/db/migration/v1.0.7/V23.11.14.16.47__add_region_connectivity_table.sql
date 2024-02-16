-- Create table
CREATE TABLE region_connectivity (
    region_connectivity_id BIGINT GENERATED ALWAYS AS IDENTITY,
    time TIMESTAMP NOT NULL DEFAULT now(),
    latency_ms INTEGER NULL,
    is_online BOOLEAN NOT NULL,
    region_id BIGINT NOT NULL,
    FOREIGN KEY (region_id) REFERENCES region(region_id) ON DELETE CASCADE,
    PRIMARY KEY (region_connectivity_id, time)
);

-- Create hypertable
-- Remove hypertable statement after migrating away from flyway
-- SELECT create_hypertable('region_connectivity','time');

-- Create index
CREATE INDEX ix_region_time ON region_connectivity (region_id, time DESC);
