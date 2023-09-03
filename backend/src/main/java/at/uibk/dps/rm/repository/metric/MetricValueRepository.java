package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the metric_value entity.
 *
 * @author matthi-g
 */
@Deprecated
public class MetricValueRepository extends Repository<MetricValue> {

    /**
     * Create an instance.
     */
    public MetricValueRepository() {
        super(MetricValue.class);
    }

    /**
     * Find a metric value by its id and fetch the metric.
     *
     * @param session the database session
     * @param metricValueId the id of the metric value
     * @return a CompletionStage that emits the metric value if it exists, else null
     */
    public CompletionStage<MetricValue> findByIdAndFetch(Session session, long metricValueId) {
        return session.createQuery("from MetricValue  mv left join fetch mv.metric " +
                "where mv.metricValueId =:metricValueId", entityClass)
            .setParameter("metricValueId", metricValueId)
            .getSingleResultOrNull();
    }

    /**
     * Find all metric values by their resource.
     *
     * @param session the database session
     * @param resourceId the id of the resource
     * @return a CompletionStage that emits a list of all metric values
     */
    public CompletionStage<List<MetricValue>> findByResourceAndFetch(Session session, long resourceId) {
        return session.createQuery("select mv from Resource r " +
                "left join r.metricValues mv " +
                "left join fetch mv.metric " +
                "where r.resourceId=:resourceId", entityClass)
            .setParameter("resourceId", resourceId)
            .getResultList();
    }

    /**
     * Find a metric value by its resource and metric.
     *
     * @param session the database session
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a CompletionStage that emits the metric value if it exists, else null
     */
    public CompletionStage<MetricValue> findByResourceAndMetric(Session session, long resourceId, long metricId) {
        return session.createQuery("select mv from Resource r " +
                "left join r.metricValues mv " +
                "where r.resourceId=:resourceId and mv.metric.metricId=:metricId", entityClass)
            .setParameter("resourceId", resourceId)
            .setParameter("metricId", metricId)
            .getSingleResultOrNull();
    }

    /**
     * Find a metric value by its resource and metric.
     *
     * @param session the database session
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a CompletionStage that emits the metric value if it exists, else null
     */
    public CompletionStage<MetricValue> findByResourceAndMetricAndFetch(Session session, long resourceId,
            long metricId) {
        return session.createQuery("select mv from Resource r " +
                "left join r.metricValues mv " +
                "left join fetch mv.metric m " +
                "left join fetch m.metricType mt " +
                "where r.resourceId=:resourceId and mv.metric.metricId=:metricId", entityClass)
            .setParameter("resourceId", resourceId)
            .setParameter("metricId", metricId)
            .getSingleResultOrNull();
    }

    /**
     * Delete a metric value by its resource and metric
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a CompletionStage that emits the row count
     */
    // TODO: maybe rework (add cascading delete to relationship)
    public CompletionStage<Integer> deleteByResourceAndMetric(Session session, long resourceId, long metricId) {
        return session.createQuery("select mv.metricValueId from Resource r " +
                "left join r.metricValues mv " +
                "where r.resourceId=:resourceId and mv.metric.metricId=:metricId", Long.class)
            .setParameter("resourceId", resourceId)
            .setParameter("metricId", metricId)
            .getSingleResult()
            .thenCompose(result -> session.createQuery("delete from MetricValue mv " +
                "where mv.metricValueId=:metricValue")
                .setParameter("metricValue", result)
                .executeUpdate());
    }
}
