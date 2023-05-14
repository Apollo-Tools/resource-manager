ALTER TABLE resource_reservation
ADD COLUMN namespace VARCHAR(64);

ALTER TABLE resource_reservation
ADD COLUMN context VARCHAR(64);
