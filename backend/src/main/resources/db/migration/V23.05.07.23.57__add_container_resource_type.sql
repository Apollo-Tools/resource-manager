INSERT INTO resource_provider (provider)
VALUES ('k8s');

INSERT INTO region (name, resource_provider_id)
VALUES ('k8s', 3);

ALTER TABLE resource_type
ALTER COLUMN resource_type TYPE VARCHAR(32)
    USING resource_type::varchar;

INSERT INTO resource_type (resource_type)
VALUES ('container')
