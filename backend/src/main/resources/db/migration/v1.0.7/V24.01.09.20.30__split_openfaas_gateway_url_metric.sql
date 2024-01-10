INSERT INTO metric (metric, description, metric_type_id, is_slo)
VALUES ('base-url', 'the base url of the resource, example: http://127.0.0.1',
        (SELECT metric_type_id FROM metric_type WHERE type = 'string'), false);
INSERT INTO metric (metric, description, metric_type_id, is_slo)
VALUES ('openfaas-port', 'the port of the openfaas gateway',
        (SELECT metric_type_id FROM metric_type WHERE type = 'number'), false);
INSERT INTO metric (metric, description, metric_type_id, is_slo)
VALUES ('metrics-port', 'the port of the node-exporter metrics endpoint',
        (SELECT metric_type_id FROM metric_type WHERE type = 'number'), false);

INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric, is_monitored)
VALUES ((SELECT platform_id FROM platform WHERE platform.platform = 'openfaas'),
        (SELECT metric_id FROM metric WHERE metric.metric = 'base-url'),
        true, true, false, false);
INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric, is_monitored)
VALUES ((SELECT platform_id FROM platform WHERE platform.platform = 'openfaas'),
        (SELECT metric_id FROM metric WHERE metric.metric = 'openfaas-port'),
        true, true, false, false);
INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric, is_monitored)
VALUES ((SELECT platform_id FROM platform WHERE platform.platform = 'openfaas'),
        (SELECT metric_id FROM metric WHERE metric.metric = 'metrics-port'),
        true, true, false, false);

UPDATE metric_value
SET metric_id = (SELECT metric_id FROM metric WHERE metric = 'base-url')
WHERE metric_id = (SELECT metric_id FROM metric WHERE metric = 'gateway-url');

DELETE FROM metric_value WHERE metric_id = (SELECT metric_id FROM metric WHERE metric = 'gateway-url');
DELETE FROM metric WHERE metric = 'gateway-url';