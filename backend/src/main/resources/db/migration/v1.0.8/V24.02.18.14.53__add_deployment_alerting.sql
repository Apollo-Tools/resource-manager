ALTER TABLE deployment
ADD COLUMN alert_notification_url text,
ADD COLUMN ensemble_id BIGINT,
ADD CONSTRAINT ensemble_id_fkey FOREIGN KEY (ensemble_id) REFERENCES ensemble (ensemble_id);
