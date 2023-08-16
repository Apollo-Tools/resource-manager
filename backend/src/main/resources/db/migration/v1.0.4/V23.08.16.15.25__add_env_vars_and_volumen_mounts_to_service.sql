CREATE TABLE env_var (
    env_var_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    value VARCHAR(512) NOT NULL,
    service_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (service_id) REFERENCES service(service_id) ON DELETE CASCADE
);

CREATE TABLE volume_mount (
    volume_mount_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    mount_path VARCHAR(512) NOT NULL,
    size_megabytes DECIMAL(16, 6) NOT NULL,
    service_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (service_id) REFERENCES service(service_id) ON DELETE CASCADE
);
