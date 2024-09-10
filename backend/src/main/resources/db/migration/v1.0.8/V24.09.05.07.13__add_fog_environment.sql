INSERT INTO environment (environment)
VALUES ('fog');

UPDATE region
SET name = 'fog'
WHERE name = 'custom-cloud';

UPDATE resource_provider
SET provider = 'custom-fog',
    environment_id = (SELECT environment_id FROM environment WHERE environment.environment = 'fog')
WHERE provider = 'custom-cloud'