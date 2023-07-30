ALTER TABLE platform_metric
    ADD COLUMN is_main_resource_metric BOOLEAN DEFAULT true NOT NULL,
    ADD COLUMN is_sub_resource_metric BOOLEAN DEFAULT false NOT NULL;

UPDATE platform_metric SET is_sub_resource_metric = true
WHERE metric_id in (
    SELECT metric_id from metric
        WHERE metric.metric in ('availability', 'online', 'cpu', 'memory-size')
    ) and platform_id in (SELECT platform_id from platform WHERE platform.platform = 'k8s');
