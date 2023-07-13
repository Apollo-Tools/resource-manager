ALTER TABLE vpc
DROP CONSTRAINT vpc_region_id_key,
ADD CONSTRAINT unique_region_created_by UNIQUE (region_id, created_by_id);
