CREATE TABLE aws_price (
    aws_price_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    region_id BIGINT NOT NULL,
    platform_id BIGINT NOT NULL,
    instance_type TEXT DEFAULT '',
    price DECIMAL(8, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (region_id, platform_id, instance_type),
    FOREIGN KEY (region_id) REFERENCES region (region_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (platform_id) REFERENCES platform (platform_id) ON UPDATE CASCADE ON DELETE CASCADE
)
