ALTER TABLE platform_metric
ADD is_monitored BOOLEAN NOT NULL DEFAULT false;

UPDATE platform_metric
SET is_monitored = true
WHERE platform_metric_id IN (
    SELECT platform_metric.platform_metric_id FROM platform_metric
    JOIN metric m on m.metric_id = platform_metric.metric_id
    JOIN platform p on p.platform_id = platform_metric.platform_id
    WHERE m.metric IN ('availability', 'latency', 'online', 'time-to-live', 'cost') AND
        p.platform = 'lambda'
    );

UPDATE platform_metric
SET is_monitored = true
WHERE platform_metric_id IN (
    SELECT platform_metric.platform_metric_id FROM platform_metric
    JOIN metric m on m.metric_id = platform_metric.metric_id
    JOIN platform p on p.platform_id = platform_metric.platform_id
    WHERE m.metric IN ('availability', 'latency', 'online', 'time-to-live', 'cost') AND
        p.platform = 'ec2'
    );

UPDATE platform_metric
SET is_monitored = true
WHERE platform_metric_id IN (
    SELECT platform_metric.platform_metric_id FROM platform_metric
    JOIN metric m on m.metric_id = platform_metric.metric_id
    JOIN platform p on p.platform_id = platform_metric.platform_id
    WHERE m.metric IN ('availability', 'latency', 'online', 'cpu', 'memory-size', 'time-to-live', 'cost') AND
        p.platform = 'openfaas'
    );

INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric, is_monitored)
VALUES ((SELECT platform_id FROM platform WHERE platform.platform = 'openfaas'),
        (SELECT metric_id FROM metric WHERE metric.metric = 'cpu-available'), true, true, false, true);
INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric, is_monitored)
VALUES ((SELECT platform_id FROM platform WHERE platform.platform = 'openfaas'),
        (SELECT metric_id FROM metric WHERE metric.metric = 'memory-size-available'), true, true, false, true);

UPDATE platform_metric
SET is_monitored = true
WHERE platform_metric_id IN (
    SELECT platform_metric.platform_metric_id FROM platform_metric
    JOIN metric m on m.metric_id = platform_metric.metric_id
    JOIN platform p on p.platform_id = platform_metric.platform_id
    WHERE m.metric IN ('cpu', 'memory-size', 'availability', 'latency', 'online', 'time-to-live', 'hostname',
        'storage-size', 'storage-size-available', 'cpu-available', 'memory-size-available', 'cost') AND
        p.platform = 'k8s'
);

ALTER TABLE metric
DROP COLUMN is_monitored;
