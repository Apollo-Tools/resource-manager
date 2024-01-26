UPDATE metric
SET description = 'The external ip of the load balancer (blank space for none)'
WHERE metric.metric = 'external-ip';
