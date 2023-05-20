CREATE TABLE service_type (
    service_type_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(64) NOT NULL UNIQUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

INSERT INTO service_type (name)
VALUES ('NodePort');
INSERT INTO service_type (name)
VALUES ('LoadBalancer');

ALTER TABLE service
ADD COLUMN image VARCHAR(64) NOT NULL DEFAULT 'alpine:latest',
ADD COLUMN replicas INTEGER NOT NULL DEFAULT 1,
ADD COLUMN ports TEXT[] NOT NULL DEFAULT array[]::varchar[],
ADD COLUMN cpu DECIMAL(3, 3) NOT NULL DEFAULT 0.1,
ADD COLUMN memory INTEGER NOT NULL DEFAULT 128,
ADD COLUMN service_type_id BIGINT NOT NULL DEFAULT 1
CONSTRAINT service_service_type_id_fkey REFERENCES service_type (service_type_id)
ON UPDATE CASCADE ON DELETE CASCADE;
