CREATE TABLE resource_type_metric (
     resource_type_metric_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     resource_type_id BIGINT NOT NULL,
     metric_id BIGINT NOT NULL,
     required BOOLEAN NOT NULL DEFAULT FALSE,
     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
     UNIQUE (resource_type_id, metric_id),
     FOREIGN KEY (resource_type_id)
         REFERENCES resource_type (type_id) ON DELETE CASCADE,
     FOREIGN KEY (metric_id)
         REFERENCES metric (metric_id) ON DELETE  CASCADE
);

CREATE TABLE vpc (
    vpc_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vpc_id_value VARCHAR(512) NOT NULL,
    subnet_id_value VARCHAR(512) NOT NULL,
    region_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (region_id),
    FOREIGN KEY (region_id)
        REFERENCES region (region_id) ON DELETE CASCADE
);