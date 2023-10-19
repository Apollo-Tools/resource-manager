UPDATE account SET role_id = (SELECT role_id FROM role WHERE role.role = 'default')
WHERE role_id IS NULL;

ALTER TABLE account
ALTER COLUMN role_id SET NOT NULL;