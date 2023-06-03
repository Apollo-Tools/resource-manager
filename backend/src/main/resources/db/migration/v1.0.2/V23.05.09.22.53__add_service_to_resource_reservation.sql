DELETE FROM resource_reservation;

ALTER TABLE resource_reservation
ADD COLUMN reservation_type VARCHAR(32) NOT NULL;

ALTER TABLE resource_reservation
ADD COLUMN resource_id BIGINT NOT NULL
CONSTRAINT resource_reservation_resource_id_fkey REFERENCES resource (resource_id)
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE resource_reservation
ADD COLUMN function_id BIGINT
CONSTRAINT resource_reservation_function_id_fkey REFERENCES function (function_id)
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE resource_reservation
ADD COLUMN service_id BIGINT
CONSTRAINT resource_reservation_servie_id_fkey REFERENCES service (service_id)
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE resource_reservation
ADD CONSTRAINT unique_function_resource UNIQUE (reservation_id, resource_id, function_id);

ALTER TABLE resource_reservation
ADD CONSTRAINT unique_service_resource UNIQUE (reservation_id, resource_id, service_id);

ALTER TABLE resource_reservation
ALTER COLUMN function_resource_id DROP NOT NULL;
