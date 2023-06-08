INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('deployment-role', 'the role necessary for deployment of the resource', false, 2);

INSERT INTO platform_metric (platform_id, metric_id, required)
VALUES ((SELECT platform_id FROM platform p WHERE p.platform = 'lambda'),
        (SELECT metric_id FROM metric m WHERE m.metric = 'deployment-role'), true);
