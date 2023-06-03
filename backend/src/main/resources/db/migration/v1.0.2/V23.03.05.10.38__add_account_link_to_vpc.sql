ALTER TABLE vpc
ADD COLUMN created_by_id BIGINT NOT NULL,
ADD CONSTRAINT created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES account (account_id);
