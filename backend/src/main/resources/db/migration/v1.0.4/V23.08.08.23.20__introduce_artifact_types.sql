CREATE TABLE artifact_type (
    artifact_type_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    artifact VARCHAR(10) NOT NULL,
    UNIQUE (name, artifact)
);

INSERT INTO artifact_type (name, artifact) VALUES ('notype', 'function');
INSERT INTO artifact_type (name, artifact) VALUES ('notype', 'service');

ALTER TABLE function
ADD COLUMN type_id BIGINT,
ADD FOREIGN KEY (type_id) REFERENCES artifact_type (artifact_type_id);

ALTER TABLE service
ADD COLUMN type_id BIGINT,
ADD FOREIGN KEY (type_id) REFERENCES artifact_type (artifact_type_id);

UPDATE function
SET type_id = (SELECT artifact_type_id FROM artifact_type WHERE artifact = 'function' AND name = 'notype');
UPDATE service
SET type_id = (SELECT artifact_type_id FROM artifact_type WHERE artifact = 'service' AND name = 'notype');

ALTER TABLE function
    ALTER COLUMN type_id SET NOT NULL;

ALTER TABLE service
    ALTER COLUMN type_id SET NOT NULL;

