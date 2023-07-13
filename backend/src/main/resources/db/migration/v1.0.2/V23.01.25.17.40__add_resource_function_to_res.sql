ALTER TABLE resource_reservation
    ADD COLUMN function_resource_id BIGINT NOT NULL;

ALTER TABLE resource_reservation
    ADD CONSTRAINT fk_function_resource FOREIGN KEY (function_resource_id)
        REFERENCES function_resource (function_resource_id);

ALTER TABLE resource_reservation
    DROP CONSTRAINT resource_reservation_resource_id_fkey;

ALTER TABLE resource_reservation
    DROP COLUMN resource_id;
