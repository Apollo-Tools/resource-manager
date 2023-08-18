-- Add new columns
ALTER TABLE resource
    ADD COLUMN name VARCHAR(256) NOT NULL DEFAULT 'resource',
    ADD COLUMN resource_type VARCHAR(4) NOT NULL DEFAULT 'main',
    ADD COLUMN subresource_id BIGINT;

-- Add constraints
ALTER TABLE resource
    ADD CONSTRAINT chk_region_platform_not_null
        CHECK(
                (resource_type = 'main' AND region_id IS NOT NULL AND platform_id IS NOT NULL)
                OR (resource_type <> 'main' AND region_id IS NULL AND platform_id IS NULL)
        ),
    ADD CONSTRAINT chk_sub_subresource_null
        CHECK (
            (resource_type = 'main' AND subresource_id IS NULL)
            OR (resource_type <> 'main' AND subresource_id IS NOT NULL) ),
    ADD CONSTRAINT unique_name_subresource UNIQUE (name, subresource_id);

-- Add foreign key
ALTER TABLE resource
    ADD FOREIGN KEY (subresource_id) REFERENCES resource (resource_id);

-- Drop old not null constraints
ALTER TABLE resource
    ALTER COLUMN region_id DROP NOT NULL,
    ALTER COLUMN platform_id DROP NOT NULL;
