ALTER TABLE resource_deployment
    ADD COLUMN base_url VARCHAR NULL,
    ADD COLUMN path VARCHAR NULL,
    ADD COLUMN metrics_port INTEGER NULL,
    ADD COLUMN openfaas_port INTEGER NULL;
