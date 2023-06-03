-- resource provider
INSERT INTO resource_provider (provider)
VALUES ('aws');
INSERT INTO resource_provider (provider)
VALUES ('edge');

-- region
INSERT INTO region (name, resource_provider_id)
VALUES ('us-east-1', 1);
INSERT INTO region (name, resource_provider_id)
VALUES ('us-west-2', 1);
INSERT INTO region (name, resource_provider_id)
VALUES ('edge', 2);

-- metric_type
INSERT INTO metric_type (type)
VALUES ('number');
INSERT INTO metric_type (type)
VALUES ('string');
INSERT INTO metric_type (type)
VALUES ('boolean');

-- resource_type
INSERT INTO resource_type (resource_type)
VALUES ('faas');
INSERT INTO resource_type (resource_type)
VALUES ('edge');
INSERT INTO resource_type (resource_type)
VALUES ('vm');

-- resource_reservation_status
INSERT INTO resource_reservation_status (status_value)
VALUES ('NEW');
INSERT INTO resource_reservation_status (status_value)
VALUES ('ERROR');
INSERT INTO resource_reservation_status (status_value)
VALUES ('DEPLOYED');
INSERT INTO resource_reservation_status (status_value)
VALUES ('TERMINATING');
INSERT INTO resource_reservation_status (status_value)
VALUES ('TERMINATED');

-- metric
-- monitored metrics
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('availability', 'the availability of a resource', true, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('latency', 'the latency between a resource and the resource manager', true, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('online', 'indicates if the resource is online', true, '3');
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('is-deployed', 'if a vm is currently deployed', true, 3);
-- configuration metrics
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('cpu', 'the amount of cpu cores of a resource', false, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('timeout', 'the maximum timeout for function executions', false, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('memory-size', 'the memory size allocated for functions', false, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('gateway-url', 'the openfaas gateway_url', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('openfaas-user', 'the openfaas basic auth user', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('openfaas-pw', 'the openfaas basic auth password', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('instance-type', 'the vm instance type', false, 2);

-- resource_type_metrics
-- faas
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (1, 1, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (1, 2, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (1, 3, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (1, 6, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (1, 7, true);
-- edge
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 1, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 2, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 3, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 6, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 7, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 8, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 9, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 10, true);
-- vm
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 1, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 2, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 4, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 6, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 7, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 8, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 9, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 10, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 11, true);

-- runtime
INSERT INTO runtime (name, template_path)
VALUES ('python3.8', './faas/python/cloud_function.py');

