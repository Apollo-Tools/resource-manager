CREATE TABLE resource_reservation_status (
    status_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    status_value VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE resource_reservation
    ADD COLUMN reservation_status_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_reservation_status FOREIGN KEY (reservation_status_id)
        REFERENCES resource_reservation_status (status_id);

CREATE TABLE log (
    log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    log_value VARCHAR(32768),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE reservation_log (
    reservation_log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    log_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (log_id, reservation_id),
    FOREIGN KEY (log_id)
        REFERENCES log (log_id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id)
        REFERENCES reservation (reservation_id) ON DELETE  CASCADE
);