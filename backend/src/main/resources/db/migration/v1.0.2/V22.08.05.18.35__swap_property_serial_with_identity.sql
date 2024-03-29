ALTER TABLE property
    ALTER COLUMN property_id SET DATA TYPE BIGINT,
    ALTER COLUMN property_id DROP DEFAULT,
    ALTER COLUMN property_id ADD GENERATED ALWAYS AS IDENTITY;

ALTER TABLE property
    RENAME COLUMN name TO property;

ALTER TABLE property_value
    ALTER COLUMN property_value_id SET DATA TYPE BIGINT,
    ALTER COLUMN property_value_id DROP DEFAULT,
    ALTER COLUMN property_value_id ADD GENERATED ALWAYS AS IDENTITY,
    ALTER COLUMN resource_id SET DATA TYPE BIGINT,
    ALTER COLUMN property_id SET DATA TYPE BIGINT;