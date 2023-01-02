CREATE TABLE account (
     account_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     username VARCHAR(32) UNIQUE NOT NULL,
     password VARCHAR(512) NOT NULL,
     is_active BOOLEAN,
     created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cloud_provider(
    provider_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider VARCHAR(8) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE credentials (
    credentials_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    access_key VARCHAR(32),
    secret_access_key VARCHAR(32),
    session_token VARCHAR(1024),
    provider_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (provider_id) REFERENCES cloud_provider (provider_id)
);

CREATE TABLE account_credentials (
    account_credentials_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL,
    credentials_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (account_id)
        REFERENCES account (account_id),
    FOREIGN KEY (credentials_id)
        REFERENCES credentials (credentials_id)
);

ALTER TABLE reservation
    ADD COLUMN created_by_id BIGINT NOT NULL,
    ADD CONSTRAINT created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES account (account_id);
