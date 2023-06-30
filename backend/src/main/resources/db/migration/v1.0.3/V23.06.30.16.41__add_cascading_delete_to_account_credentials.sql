ALTER TABLE account_credentials
DROP CONSTRAINT account_credentials_account_id_fkey,
DROP CONSTRAINT account_credentials_credentials_id_fkey,
ADD FOREIGN KEY (account_id) REFERENCES account (account_id) ON DELETE CASCADE,
ADD FOREIGN KEY (credentials_id) REFERENCES credentials (credentials_id) ON DELETE CASCADE;
