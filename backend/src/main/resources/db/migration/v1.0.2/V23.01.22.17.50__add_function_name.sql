ALTER TABLE function
ADD COLUMN name VARCHAR(64) NOT NULL DEFAULT '';

ALTER TABLE function
ADD CONSTRAINT unique_function_name UNIQUE (name, runtime_id);
