INSERT INTO metric (metric, description, metric_type_id)
VALUES ('docker-architecture', 'the docker architecture of the resource e.g. linux/amd64, linux/arm/v7.',
        (SELECT metric_type_id FROM metric_type WHERE type='string'));

INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric,
                             is_monitored)
VALUES ((SELECT platform_id FROM platform WHERE platform='ec2'),
        (SELECT metric_id FROM metric WHERE metric='docker-architecture'),
        true, true, false, false
);
INSERT INTO platform_metric (platform_id, metric_id, required, is_main_resource_metric, is_sub_resource_metric,
                             is_monitored)
VALUES ((SELECT platform_id FROM platform WHERE platform='openfaas'),
        (SELECT metric_id FROM metric WHERE metric='docker-architecture'),
        true, true, false, false
);
