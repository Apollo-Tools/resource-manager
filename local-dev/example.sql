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

-- resource_type
INSERT INTO resource_type (resource_type)
VALUES ('faas');
INSERT INTO resource_type (resource_type)
VALUES ('edge');
INSERT INTO resource_type (resource_type)
VALUES ('vm');
INSERT INTO resource_type (resource_type)
VALUES ('iot');

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

-- region
INSERT INTO region (name, resource_provider_id)
VALUES ('eu-north', 1);
INSERT INTO region (name, resource_provider_id)
VALUES ('us-east-1', 1);
INSERT INTO region (name, resource_provider_id)
VALUES ('us-west-2', 1);
INSERT INTO region (name, resource_provider_id)
VALUES ('edge', 5);

-- vpc
INSERT INTO vpc (vpc_id_value, subnet_id_value, region_id, created_by_id)
VALUES ('vpc-034ecb3faf855301a', 'subnet-02c1ba2560529be72', 2, 1);
INSERT INTO vpc (vpc_id_value, subnet_id_value, region_id, created_by_id)
VALUES ('vpc-03e37d94124ae821c', 'subnet-02109321bd7f82080', 3, 1);

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
VALUES (10, false, 1, 3);
INSERT INTO metric_value (count, value_bool, resource_id, metric_id)
VALUES (10, false, 2, 3);
INSERT INTO metric_value (count, value_bool, resource_id, metric_id)
VALUES (10, true, 3, 3);

-- is-deployed
INSERT INTO metric_value (count, value_bool, resource_id, metric_id)
VALUES (10, true, 3, 4);

-- cpu
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (16, 3, 5);

-- timeout
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (450, 1, 6);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (400, 2, 6);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (500, 3, 6);

-- memory-size
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (512, 1, 7);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (128, 2, 7);
INSERT INTO metric_value (value_number, resource_id, metric_id)
VALUES (1024, 3, 7);

-- gateway-url
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('http://192.168.10.131:8080', 2, 8);
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('', 3, 8);

-- openfaas-user
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('admin', 2, 9);
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('', 3, 9);

-- openfaas-pw
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('123', 2, 10);
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('', 3, 10);

-- instance-type
INSERT INTO metric_value (value_string, resource_id, metric_id)
VALUES ('t2.micro', 3, 11);


-- resource_reservation_status
INSERT INTO resource_reservation_status (status_value)
VALUES ('NEW');
INSERT INTO resource_reservation_status (status_value)
VALUES ('ERROR');
INSERT INTO resource_reservation_status (status_value)
VALUES ('DEPLOYED');
INSERT INTO resource_reservation_status (status_value)
VALUES ('TERMINATED');
