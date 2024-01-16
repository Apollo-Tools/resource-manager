UPDATE platform_metric
SET is_monitored = false
WHERE metric_id = (SELECT metric_id FROM metric WHERE metric.metric = 'storage') and
        platform_id = (SELECT platform_id FROM platform WHERE platform.platform in ('ec2'));

DELETE FROM metric_value
WHERE metric_value_id in (SELECT mv.metric_value_id FROM metric_value mv
    LEFT JOIN platform_metric pm ON pm.metric_id = mv.metric_id
    WHERE pm.is_monitored);
