ALTER TABLE metric
    ADD COLUMN is_monitored BOOLEAN NOT NULL default false;

DROP TABLE property_value;
DROP TABLE property;