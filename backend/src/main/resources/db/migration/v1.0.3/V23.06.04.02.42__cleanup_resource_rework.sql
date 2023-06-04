ALTER TABLE resource
DROP COLUMN is_self_managed,
DROP COLUMN resource_type;

UPDATE region SET resource_provider_id = (SELECT provider_id FROM resource_provider WHERE provider = 'custom-cloud')
WHERE name = 'private_cloud';
