CREATE TABLE role (
    role_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (role)
);

INSERT INTO role (role) VALUES ('admin');
INSERT INTO role (role) VALUES ('default');

ALTER TABLE account
    ADD COLUMN role_id BIGINT;

ALTER TABLE account
    ADD FOREIGN KEY (role_id) REFERENCES role (role_id);

UPDATE account SET role_id = (SELECT role_id FROM role WHERE role.role = 'default');

ALTER TABLE role
    ALTER COLUMN role_id SET NOT NULL;
