CREATE TABLE region (
    region_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(512),
    resource_provider_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (resource_provider_id) REFERENCES resource_provider (provider_id)
);

ALTER TABLE resource
    ADD COLUMN region_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_region FOREIGN KEY (region_id)
        REFERENCES region (region_id);

ALTER TABLE region
    ADD CONSTRAINT unique_provider_region UNIQUE (name, resource_provider_id);
