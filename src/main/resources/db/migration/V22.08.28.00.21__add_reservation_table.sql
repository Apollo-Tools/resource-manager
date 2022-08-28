CREATE TABLE reservation (
     reservation_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     is_active BOOLEAN,
     created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE resource_reservation(
     resource_reservation_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     is_deployed BOOLEAN,
     resource_id BIGINT NOT NULL,
     reservation_id BIGINT NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
     FOREIGN KEY (resource_id)
         REFERENCES resource (resource_id),
     FOREIGN KEY (reservation_id)
         REFERENCES reservation (reservation_id)
);

ALTER TABLE resource
DROP COLUMN is_reserved;

ALTER TABLE resource
RENAME COLUMN is_deployed to is_self_managed;