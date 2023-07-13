-- metric
-- configuration metrics
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('cluster-url', 'the url of k8s cluster', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('replicas', 'the amount of replicas', false, 1);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('ports', 'the ports to expose with the schema container_port:service_port separated by ;', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('service-type', 'Whether the service should be of type NodePort or LoadBalancer', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('external-ip', 'The external ip of the load balancer', false, 2);
INSERT INTO metric (metric, description, is_monitored, metric_type_id)
VALUES ('pre-pull-timeout', 'The timeout for the pre pull daemonset in minutes', false, 1);

-- resource_type_metrics
-- container
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 5, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 7, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 12, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 13, true);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 14, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 15, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 16, false);
INSERT INTO resource_type_metric (resource_type_id, metric_id, required)
VALUES (4, 17, true);
