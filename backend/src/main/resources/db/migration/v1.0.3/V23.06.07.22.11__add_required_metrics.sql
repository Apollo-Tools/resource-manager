INSERT INTO platform_metric (platform_id, metric_id, required)
VALUES ((SELECT platform_id FROM platform p WHERE p.platform = 'openfaas'),
        (SELECT metric_id FROM metric m WHERE m.metric = 'gateway-url'), true),
    ((SELECT platform_id FROM platform p WHERE p.platform = 'openfaas'),
        (SELECT metric_id FROM metric m WHERE m.metric = 'openfaas-user'), true),
    ((SELECT platform_id FROM platform p WHERE p.platform = 'openfaas'),
        (SELECT metric_id FROM metric m WHERE m.metric = 'openfaas-pw'), true);
