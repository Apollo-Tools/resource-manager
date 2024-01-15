UPDATE metric
SET metric = 'up'
WHERE metric.metric = 'online';

UPDATE metric
SET metric = 'node', description = 'the name of a k8s node'
WHERE metric.metric = 'hostname';

UPDATE metric
SET metric = 'cpu%', description = 'the cpu utilisation in percent'
WHERE metric.metric = 'cpu-available';

UPDATE metric
SET metric = 'memory', description = 'the memory size of a resource in bytes'
WHERE metric.metric = 'memory-size';

UPDATE metric
SET metric = 'memory%', description = 'the memory utilisation in percent'
WHERE metric.metric = 'memory-size-available';

UPDATE metric
SET metric = 'storage', description = 'the storage size of a resource in bytes'
WHERE metric.metric = 'storage-size';

UPDATE metric
SET metric = 'storage%', description = 'the storage utilisation in percent'
WHERE metric.metric = 'storage-size-available';

DELETE FROM metric_value
WHERE metric_id = (SELECT metric_id FROM metric WHERE metric.metric = 'docker-architecture');

DELETE FROM metric
WHERE metric.metric = 'docker-architecture';

ALTER TABLE platform_metric
    DROP COLUMN required,
    ADD COLUMN monitor_during_deployment BOOLEAN DEFAULT false;

UPDATE platform_metric
SET monitor_during_deployment = true
WHERE metric_id in (SELECT metric_id FROM metric WHERE metric.metric in ('up', 'latency', 'cpu%', 'memory%',
                                                                         'storage%'));

UPDATE platform_metric
SET is_sub_resource_metric = true
WHERE metric_id in (SELECT metric_id FROM metric WHERE metric.metric in ('latency', 'cost', 'time-to-live')) and
      platform_id = (SELECT platform_id FROM platform WHERE platform.platform = 'k8s');

UPDATE platform_metric
SET is_monitored = false
WHERE metric_id = (SELECT metric_id FROM metric WHERE metric.metric = 'cost') and
      platform_id in (SELECT platform_id FROM platform WHERE platform.platform in ('k8s', 'openfaas'));

INSERT INTO platform_metric (platform_id, metric_id, is_main_resource_metric, is_sub_resource_metric, is_monitored,
                             monitor_during_deployment)
VALUES((SELECT platform_id FROM platform WHERE platform.platform='lambda'),
       (SELECT metric_id FROM metric WHERE metric.metric='memory'),
       true, false, false, false);

DELETE FROM platform_metric
WHERE platform_id in (SELECT platform_id FROM platform WHERE platform.platform != 'k8s') and
      metric_id = (SELECT metric_id FROM metric WHERE metric.metric = 'time-to-live');

DELETE FROM platform_metric
WHERE platform_id = (SELECT platform_id FROM platform WHERE platform.platform = 'k8s') and
      metric_id in (SELECT metric_id FROM metric WHERE metric.metric in ('storage', 'storage%'));

INSERT INTO platform_metric (platform_id, metric_id, is_main_resource_metric, is_sub_resource_metric, is_monitored,
                             monitor_during_deployment)
VALUES((SELECT platform_id FROM platform WHERE platform.platform='ec2'),
       (SELECT metric_id FROM metric WHERE metric.metric='storage'),
       true, false, true, false);

INSERT INTO platform_metric (platform_id, metric_id, is_main_resource_metric, is_sub_resource_metric, is_monitored,
                             monitor_during_deployment)
VALUES((SELECT platform_id FROM platform WHERE platform.platform='openfaas'),
       (SELECT metric_id FROM metric WHERE metric.metric='storage'),
       true, false, true, false);

INSERT INTO platform_metric (platform_id, metric_id, is_main_resource_metric, is_sub_resource_metric, is_monitored,
                             monitor_during_deployment)
VALUES((SELECT platform_id FROM platform WHERE platform.platform='ec2'),
       (SELECT metric_id FROM metric WHERE metric.metric='storage%'),
       true, false, true, true);

INSERT INTO platform_metric (platform_id, metric_id, is_main_resource_metric, is_sub_resource_metric, is_monitored,
                             monitor_during_deployment)
VALUES((SELECT platform_id FROM platform WHERE platform.platform='openfaas'),
       (SELECT metric_id FROM metric WHERE metric.metric='storage%'),
       true, false, true, true);

INSERT INTO platform_metric (platform_id, metric_id, is_main_resource_metric, is_sub_resource_metric, is_monitored,
                             monitor_during_deployment)
VALUES((SELECT platform_id FROM platform WHERE platform.platform='ec2'),
       (SELECT metric_id FROM metric WHERE metric.metric='cpu%'),
       true, false, true, true);

INSERT INTO platform_metric (platform_id, metric_id, is_main_resource_metric, is_sub_resource_metric, is_monitored,
                             monitor_during_deployment)
VALUES((SELECT platform_id FROM platform WHERE platform.platform='ec2'),
       (SELECT metric_id FROM metric WHERE metric.metric='memory%'),
       true, false, true, true);
