CREATE TABLE property (
                        property_id SERIAL PRIMARY KEY,
                        name VARCHAR(256) NOT NULL,
                        description VARCHAR (512),
                        created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE property_value (
                              property_value_id SERIAL PRIMARY KEY,
                              value_string VARCHAR(2048),
                              value_number DECIMAL(20, 10),
                              value_bool BOOLEAN,
                              is_unique BOOLEAN NOT NULL DEFAULT false,
                              resource_id INT NOT NULL,
                              property_id INT NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              FOREIGN KEY (resource_id)
                                  REFERENCES resource (resource_id),
                              FOREIGN KEY (property_id)
                                  REFERENCES property (property_id)
);

ALTER TABLE resource DROP COLUMN url;
ALTER TABLE resource ADD COLUMN is_deployed BOOLEAN DEFAULT false;
ALTER TABLE resource ADD COLUMN is_reserved BOOLEAN DEFAULT false;
