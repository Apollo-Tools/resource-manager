ALTER TABLE function
ADD COLUMN timeout_seconds SMALLINT NOT NULL DEFAULT 60,
ADD COLUMN memory_megabytes SMALLINT NOT NULL DEFAULT 128;

DELETE FROM metric_value as mv
WHERE mv.resource_id in
    (
        SELECT resource_id from resource
        left join public.platform p on resource.platform_id = p.platform_id
        where p.platform = 'lambda'
    )
and mv.metric_id in
    (
        SELECT metric_id from metric
        WHERE metric in ('timeout', 'memory-size')
    );

DELETE FROM platform_metric as pm
WHERE pm.platform_id =
      (
          SELECT platform_id from platform
          WHERE platform.platform = 'lambda'
      )
  and pm.metric_id in
      (
          SELECT metric_id from metric
          WHERE metric in ('timeout', 'memory-size')
      );
