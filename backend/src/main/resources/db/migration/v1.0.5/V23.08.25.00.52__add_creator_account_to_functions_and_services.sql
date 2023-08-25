ALTER TABLE function
ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN created_by_id BIGINT,
ADD CONSTRAINT created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES account (account_id) ON DELETE CASCADE,
DROP CONSTRAINT unique_function_name,
ADD CONSTRAINT unique_function_name unique(type_id, runtime_id, created_by_id, name);

ALTER TABLE service
ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN created_by_id BIGINT,
ADD CONSTRAINT created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES account (account_id) ON DELETE CASCADE,
DROP CONSTRAINT service_name_key,
ADD CONSTRAINT unique_service_name unique(type_id, created_by_id, name);

UPDATE function SET created_by_id=(
    SELECT account_id FROM account left join role r on r.role_id = account.role_id WHERE r.role = 'admin' LIMIT 1
), is_public = true;
UPDATE service SET created_by_id=(
    SELECT account_id FROM account left join role r on r.role_id = account.role_id WHERE r.role = 'admin' LIMIT 1
), is_public = true;

ALTER TABLE function
ALTER COLUMN created_by_id SET NOT NULL;
ALTER TABLE service
ALTER COLUMN created_by_id SET NOT NULL;
