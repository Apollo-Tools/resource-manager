ALTER TABLE function_deployment_exec_time DROP CONSTRAINT function_deployment_exec_time_pkey;

ALTER TABLE function_deployment_exec_time
ADD CONSTRAINT function_deployment_exec_time_pkey PRIMARY KEY (exec_time_id, time);

