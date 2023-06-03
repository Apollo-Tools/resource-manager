-- Add new metrics
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('cost', 'the estimated cost per second', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('time-to-live', 'the time span in seconds until a resource is deployed', false, 2);

-- Remove monitored state from metrics for now
UPDATE metric SET is_monitored = false  WHERE metric_id in (1,2,3,4);

-- Add cost and time-to-live to faas
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (1, 18, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (1, 19, false);

-- Add cpu, cost and time-to-live to edge
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 5, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 18, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (2, 19, false);

-- Add cpu, cost and time-to-live to vm
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 5, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 18, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (3, 19, false);

-- Add metrics to container resources
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 2, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 3, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 5, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 7, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 18, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 19, false);
