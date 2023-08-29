-- Cleanup of unused metrics
DELETE FROM metric WHERE metric IN ('is-deployed', 'replicas', 'ports', 'service-type');

ALTER TABLE metric
ADD COLUMN is_slo BOOLEAN NOT NULL DEFAULT false;

UPDATE metric SET is_slo = true
WHERE metric.metric IN ('availability','latency','online','cpu', 'memory-size', 'instance-type', 'pre-pull-timeout',
    'cost','time-to-live','hostname','storage-size','storage-size-available', 'cpu-available', 'memory-size-available',
    'docker-architecture');

DELETE FROM ensemble_slo WHERE name IN (SELECT metric FROM metric WHERE metric.is_slo = false);