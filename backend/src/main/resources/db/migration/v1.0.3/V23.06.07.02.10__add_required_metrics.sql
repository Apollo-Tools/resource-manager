INSERT INTO platform_metric (platform_id, metric_id, required)
VALUES ((SELECT platform_id FROM platform p WHERE p.platform = 'ec2'),
        (SELECT metric_id FROM metric m WHERE m.metric = 'instance-type'),
        true);
