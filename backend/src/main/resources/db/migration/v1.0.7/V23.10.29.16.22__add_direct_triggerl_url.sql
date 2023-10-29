ALTER TABLE resource_deployment
RENAME COLUMN trigger_url TO rm_trigger_url;

ALTER TABLE resource_deployment
ADD COLUMN direct_trigger_url VARCHAR(1024) DEFAULT '';

UPDATE resource_deployment as rd SET direct_trigger_url = rd.rm_trigger_url WHERE deployment_type = 'function';
