INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('hostname', 'the value of the hostname label of a k8s node', false, 2);

INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric)
VALUES ((SELECT platform_id FROM platform p WHERE p.platform = 'k8s'),
        (SELECT metric_id FROM metric m WHERE m.metric = 'hostname'), true, false, true);
