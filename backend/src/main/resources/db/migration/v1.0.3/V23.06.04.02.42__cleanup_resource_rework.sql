ALTER TABLE resource
DROP COLUMN is_self_managed,
DROP COLUMN resource_type;

UPDATE region SET resource_provider_id = (SELECT provider_id FROM resource_provider WHERE provider = 'custom-cloud')
WHERE name = 'private-cloud';

CREATE TABLE provider_platform (
    provider_platform_id BIGINT GENERATED ALWAYS AS IDENTITY primary key,
    provider_id BIGINT NOT NULL,
    platform_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, platform_id),
    FOREIGN KEY (provider_id)
    REFERENCES resource_provider (provider_id) ON DELETE CASCADE,
    FOREIGN KEY (platform_id)
    REFERENCES platform (platform_id) ON DELETE CASCADE
);

-- aws
INSERT INTO provider_platform (provider_id, platform_id)
VALUES ((SELECT provider_id FROM resource_provider WHERE provider = 'aws'),
        (SELECT platform_id FROM platform WHERE platform = 'lambda')),
       ((SELECT provider_id FROM resource_provider WHERE provider = 'aws'),
        (SELECT platform_id FROM platform WHERE platform = 'ec2')),
       ((SELECT provider_id FROM resource_provider WHERE provider = 'aws'),
        (SELECT platform_id FROM platform WHERE platform = 'openfaas')),
       ((SELECT provider_id FROM resource_provider WHERE provider = 'aws'),
        (SELECT platform_id FROM platform WHERE platform = 'k8s'));
-- custom-cloud
INSERT INTO provider_platform (provider_id, platform_id)
VALUES ((SELECT provider_id FROM resource_provider WHERE provider = 'custom-cloud'),
        (SELECT platform_id FROM platform WHERE platform = 'openfaas')),
       ((SELECT provider_id FROM resource_provider WHERE provider = 'custom-cloud'),
        (SELECT platform_id FROM platform WHERE platform = 'k8s'));
-- custom-cloud
INSERT INTO provider_platform (provider_id, platform_id)
VALUES ((SELECT provider_id FROM resource_provider WHERE provider = 'custom-edge'),
        (SELECT platform_id FROM platform WHERE platform = 'openfaas')),
       ((SELECT provider_id FROM resource_provider WHERE provider = 'custom-edge'),
        (SELECT platform_id FROM platform WHERE platform = 'k8s'));
