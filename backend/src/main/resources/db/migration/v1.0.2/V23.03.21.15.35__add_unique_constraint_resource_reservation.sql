ALTER TABLE resource_reservation
    ADD CONSTRAINT unique_resource_reservation UNIQUE (reservation_id, function_resource_id);
