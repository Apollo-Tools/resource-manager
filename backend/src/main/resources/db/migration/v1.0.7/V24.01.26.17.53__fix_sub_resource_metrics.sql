DELETE FROM metric
WHERE metric.metric = 'node';

UPDATE platform_metric
SET is_sub_resource_metric=false
WHERE metric_id = (SELECT metric_id FROM metric WHERE metric.metric='cost')
    AND platform_id = (SELECT platform_id FROM platform WHERE platform.platform='k8s');