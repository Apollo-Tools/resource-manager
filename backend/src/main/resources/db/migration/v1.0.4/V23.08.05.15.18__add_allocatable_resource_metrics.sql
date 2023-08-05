INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('storage-size', 'the amount of total storage in bytes', true,
        (SELECT metric_type_id FROM metric_type WHERE type = 'number'));
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('storage-size-available', 'the amount of allocatable storage in bytes', true,
        (SELECT metric_type_id FROM metric_type WHERE type = 'number'));
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('cpu-available', 'the amount of allocatable cpu units', true,
        (SELECT metric_type_id FROM metric_type WHERE type = 'number'));
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('memory-size-available', 'the amount of allocatable memory in bytes', true,
        (SELECT metric_type_id FROM metric_type WHERE type = 'number'));

INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric)
VALUES ((SELECT platform_id FROM platform WHERE platform = 'k8s'),
        (SELECT metric_id FROM metric WHERE metric = 'storage-size'), false, true, true);
INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric)
VALUES ((SELECT platform_id FROM platform WHERE platform = 'k8s'),
        (SELECT metric_id FROM metric WHERE metric = 'storage-size-available'), false, true, true);
INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric)
VALUES ((SELECT platform_id FROM platform WHERE platform = 'k8s'),
        (SELECT metric_id FROM metric WHERE metric = 'cpu-available'), false, true, true);
INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric)
VALUES ((SELECT platform_id FROM platform WHERE platform = 'k8s'),
        (SELECT metric_id FROM metric WHERE metric = 'memory-available'), false, true, true);
