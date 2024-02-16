-- Create table
CREATE TABLE function_deployment_exec_time (
    exec_time_id BIGINT GENERATED ALWAYS AS IDENTITY,
    time TIMESTAMP NOT NULL,
    exec_time_ms INTEGER NOT NULL,
    resource_deployment_id BIGINT NOT NULL,
    FOREIGN KEY (resource_deployment_id) REFERENCES resource_deployment(resource_deployment_id) ON DELETE CASCADE,
    PRIMARY KEY (exec_time_id, time)
);

-- Create hypertable
-- Remove hypertable statement after migrating away from flyway
-- SELECT create_hypertable('function_deployment_exec_time','time');

-- Create index
CREATE INDEX ix_exec_time_time ON function_deployment_exec_time (resource_deployment_id, time DESC);
