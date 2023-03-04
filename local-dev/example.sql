-- account
-- password == password1
INSERT INTO account (username, password, is_active)
VALUES ('user1', '$argon2i$v=19$m=15,t=2,p=1$FLjLOen0guXBQXGtA6p2Qw$ybqyFx0/vUUxrrgYQm3Cje/G46cp/uTMaPAR6/Q1frA', true);
INSERT INTO account (username, password, is_active)
-- password == password1
VALUES ('user2', '$argon2i$v=19$m=15,t=2,p=1$FLjLOen0guXBQXGtA6p2Qw$ybqyFx0/vUUxrrgYQm3Cje/G46cp/uTMaPAR6/Q1frA', true);

-- resource provider
INSERT INTO resource_provider (provider)
VALUES ('aws');
INSERT INTO resource_provider (provider)
VALUES ('azure');
INSERT INTO resource_provider (provider)
VALUES ('google');
INSERT INTO resource_provider (provider)
VALUES ('ibm');
INSERT INTO resource_provider (provider)
VALUES ('edge');

-- metric_type
INSERT INTO metric_type (type)
VALUES ('number');
INSERT INTO metric_type (type)
VALUES ('string');
INSERT INTO metric_type (type)
VALUES ('boolean');

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
VALUES ('timeout', 'the maximum timeout for function executions', false, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('memory-size', 'the memory size allocated for functions', false, 1);

-- resource_type
INSERT INTO resource_type (resource_type)
VALUES ('faas');
INSERT INTO resource_type (resource_type)
VALUES ('edge');
INSERT INTO resource_type (resource_type)
VALUES ('vm');
INSERT INTO resource_type (resource_type)
VALUES ('iot');

-- region
INSERT INTO region (name, resource_provider_id)
VALUES ('eu-north', 1);
INSERT INTO region (name, resource_provider_id)
VALUES ('us-east-1', 1);
INSERT INTO region (name, resource_provider_id)
VALUES ('us-west-2', 1);
INSERT INTO region (name, resource_provider_id)
VALUES ('edge', 5);

-- resource
-- faas
INSERT INTO resource (resource_type, is_self_managed, region_id)
VALUES (1, false, 3);
-- edge
INSERT INTO resource(resource_type, is_self_managed, region_id)
VALUES (2, true, 2);
-- vm
INSERT INTO resource(resource_type, is_self_managed, region_id)
VALUES (3, false, 3);

-- runtime
INSERT INTO runtime (name, template_path)
VALUES ('python3.8', './faas/python/cloud_function.py');

-- function
INSERT INTO function (name, runtime_id, code)
VALUES ('add1', 1, 'def main(json_input):
  input1 = json_input["input1"]
  res = {
    "output": input1 + 1
  }
  return res
');

INSERT INTO function (name, runtime_id, code)
VALUES ('sub1', 1, 'def main(json_input):
  input1 = json_input["input1"]
  res = {
    "output": input1 - 1
  }
  return res
');

-- function_resource
INSERT INTO function_resource (function_id, resource_id, is_deployed)
VALUES (1, 1, false);
INSERT INTO function_resource (function_id, resource_id, is_deployed)
VALUES (1, 2, false);
INSERT INTO function_resource (function_id, resource_id, is_deployed)
VALUES (1, 3, false);
INSERT INTO function_resource (function_id, resource_id, is_deployed)
VALUES (2, 3, false);

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

-- timeout
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (450, 1, 5);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (400, 2, 5);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (500, 3, 5);

-- memory-size
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (512, 1, 6);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (128, 2, 6);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (1024, 3, 6);

-- resource_reservation_status
INSERT INTO resource_reservation_status (status_value)
VALUES ('NEW');
INSERT INTO resource_reservation_status (status_value)
VALUES ('ERROR');
INSERT INTO resource_reservation_status (status_value)
VALUES ('DEPLOYED');
INSERT INTO resource_reservation_status (status_value)
VALUES ('TERMINATED');
