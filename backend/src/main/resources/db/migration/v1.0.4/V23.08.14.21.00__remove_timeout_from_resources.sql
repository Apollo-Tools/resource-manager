DELETE FROM metric_value as mv
WHERE mv.metric_id in
    (
      SELECT metric_id from metric
      WHERE metric = 'timeout'
    );

DELETE FROM metric as m
WHERE m.metric = 'timeout';
