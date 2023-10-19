ALTER TABLE resource
RENAME COLUMN subresource_id TO main_resource_id;

ALTER TABLE resource
DROP CONSTRAINT unique_name_mainresource;

CREATE UNIQUE INDEX unique_name_main_resource ON resource (name)
    WHERE resource.main_resource_id IS NULL;
