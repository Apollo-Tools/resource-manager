-- metric_type
INSERT INTO metric_type (type)
VALUES ('number');
INSERT INTO metric_type (type)
VALUES ('string');
INSERT INTO metric_type (type)
VALUES ('bool');

-- metric
-- monitored metrics
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('availability', 'the availability of a resource', true, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('latency', 'the latency between a resource and the resource manager', true, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('online', 'indicates if the resource is online', true, '3');
-- configuration metrics
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('cpu', 'the amount of cpu cores of a resource', false, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('region', 'the region of a resource', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('trigger_url', 'the url of the deployed function', false, 2);

-- resource_type
INSERT INTO resource_type (resource_type)
VALUES ('faas');
INSERT INTO resource_type (resource_type)
VALUES ('edge');
INSERT INTO resource_type (resource_type)
VALUES ('vm');
INSERT INTO resource_type (resource_type)
VALUES ('iot');

-- resource
-- faas
INSERT INTO resource (resource_type, is_deployed, is_reserved)
VALUES (1, false, false);
-- edge
INSERT INTO resource(resource_type, is_deployed, is_reserved)
VALUES (2, true, false);
-- vm
INSERT INTO resource(resource_type, is_deployed, is_reserved)
VALUES (3, false, false);


-- metric_value
-- availability
INSERT INTO metric_value (count, value_number, resource_id, metric_id)
VALUES (10, 0.998, 1, 1);
INSERT INTO metric_value (count, value_number, resource_id, metric_id)
VALUES (10, 0.999, 2, 1);
INSERT INTO metric_value (count, value_number, resource_id, metric_id)
VALUES (10, 0.99, 3, 1);

-- latency
INSERT INTO metric_value (count, value_number, resource_id, metric_id)
VALUES (50, 43, 1, 2);
INSERT INTO metric_value (count, value_number, resource_id, metric_id)
VALUES (50, 80, 2, 2);
INSERT INTO metric_value (count, value_number, resource_id, metric_id)
VALUES (50, 20, 3, 2);

-- online
INSERT INTO metric_value (count, value_bool, resource_id, metric_id)
VALUES (10, true, 1, 3);
INSERT INTO metric_value (count, value_bool, resource_id, metric_id)
VALUES (10, false, 2, 3);
INSERT INTO metric_value (count, value_bool, resource_id, metric_id)
VALUES (10, true, 3, 3);

-- cpu
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (8, 1, 4);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (4, 2, 4);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (16, 3, 4);

-- region
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('eu-north', 1, 5);
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('eu-east', 2, 5);
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('eu-west', 3, 5);

-- trigger_url
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('https://function1.url', 1, 6);
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('https://function2.url', 2, 6);
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('https://function3.url', 3, 6);

