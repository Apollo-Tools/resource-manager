UPDATE resource SET name = name || resource_id || region_id
    WHERE resource_type = 'main';

ALTER TABLE resource
    ADD CONSTRAINT unique_name_mainresource UNIQUE (name, region_id);
