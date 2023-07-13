-- Remove all resource types other than 'faas' or 'container'
UPDATE resource SET resource_type = (SELECT type_id FROM resource_type WHERE resource_type = 'faas')
    WHERE resource_type IN (SELECT type_id FROM resource_type WHERE resource_type = 'edge' OR resource_type = 'vm');
DELETE FROM resource_type WHERE resource_type != 'faas' AND resource_type != 'container';

-- Add platform
CREATE TABLE platform (
    platform_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    platform VARCHAR(64) NOT NULL UNIQUE,
    resource_type_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (resource_type_id) REFERENCES resource_type(type_id) ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT INTO platform (platform, resource_type_id)
VALUES ('lambda', (SELECT type_id FROM resource_type WHERE resource_type = 'faas'));
INSERT INTO platform (platform, resource_type_id)
VALUES ('ec2', (SELECT type_id FROM resource_type WHERE resource_type = 'faas'));
INSERT INTO platform (platform, resource_type_id)
VALUES ('openfaas', (SELECT type_id FROM resource_type WHERE resource_type = 'faas'));
INSERT INTO platform (platform, resource_type_id)
VALUES ('k8s', (SELECT type_id FROM resource_type WHERE resource_type = 'container'));

-- Update resource
ALTER TABLE resource
ADD COLUMN platform_id bigint,
ADD CONSTRAINT resource_platform_fkey FOREIGN KEY (platform_id) REFERENCES platform (platform_id)
    ON UPDATE CASCADE ON DELETE CASCADE;
UPDATE resource SET platform_id = (SELECT platform.platform_id FROM platform WHERE platform = 'lambda');
ALTER TABLE resource ALTER COLUMN platform_id SET NOT NULL;

-- Rename k8s region to private-cloud
UPDATE region SET name = 'private-cloud' WHERE name = 'k8s';

-- Add environment
CREATE TABLE environment (
    environment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    environment VARCHAR(32) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO environment (environment) VALUES ('cloud');
INSERT INTO environment (environment) VALUES ('edge');

ALTER TABLE resource_provider
ADD COLUMN environment_id bigint,
ADD CONSTRAINT resource_provider_environment FOREIGN KEY (environment_id) REFERENCES environment (environment_id)
    ON UPDATE CASCADE ON DELETE CASCADE;
UPDATE resource_provider
SET environment_id = (SELECT environment.environment_id FROM environment WHERE environment = 'cloud');
ALTER TABLE resource_provider ALTER COLUMN environment_id SET NOT NULL;

-- Update resource provider
ALTER TABLE resource_provider
ALTER COLUMN provider TYPE VARCHAR(16);
INSERT INTO resource_provider (provider, environment_id)
VALUES ('custom-cloud', (SELECT environment_id FROM environment WHERE environment = 'cloud')),
       ('custom-edge', (SELECT environment_id FROM environment WHERE environment = 'edge'));
UPDATE region
SET resource_provider_id = (SELECT rp.provider_id FROM resource_provider rp WHERE provider = 'custom-edge')
WHERE resource_provider_id IN (SELECT rp.provider_id FROM resource_provider rp WHERE provider = 'edge' OR provider = 'k8s');
DELETE FROM resource_provider WHERE provider = 'k8s' OR provider = 'edge';
