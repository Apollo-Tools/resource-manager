ALTER TABLE resource_reservation
    ADD CONSTRAINT unique_resource_reservation_mapping UNIQUE (resource_id, reservation_id);

ALTER TABLE account_credentials
    ADD CONSTRAINT unique_account_credentials_mapping UNIQUE (account_id, credentials_id);
