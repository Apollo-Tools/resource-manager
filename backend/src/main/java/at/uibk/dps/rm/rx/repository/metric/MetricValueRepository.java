package at.uibk.dps.rm.rx.repository.metric;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the metric_value entity.
 *
 * @author matthi-g
 */
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
     * @param sessionManager the database session manager
     * @param metricValueId the id of the metric value
     * @return a Maybe that emits the metric value if it exists, else null
     */
    public Maybe<MetricValue> findByIdAndFetch(SessionManager sessionManager, long metricValueId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from MetricValue  mv left join fetch mv" +
                ".metric " +
                "where mv.metricValueId =:metricValueId", entityClass)
            .setParameter("metricValueId", metricValueId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all metric values by their resource.
     *
     * @param sessionManager the database session manager
     * @param resourceId the id of the resource
     * @return a Single that emits a list of all metric values
     */
    public Single<List<MetricValue>> findAllByResourceAndFetch(SessionManager sessionManager, long resourceId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select mv from Resource r " +
                "left join r.metricValues mv " +
                "left join fetch mv.metric " +
                "where r.resourceId=:resourceId", entityClass)
            .setParameter("resourceId", resourceId)
            .getResultList()
        );
    }

    /**
     * Find a metric value by its resource and metric.
     *
     * @param sessionManager the database session manager
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a Maybe that emits the metric value if it exists, else null
     */
    public Maybe<MetricValue> findByResourceAndMetric(SessionManager sessionManager, long resourceId, long metricId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select mv from Resource r " +
                "left join r.metricValues mv " +
                "where r.resourceId=:resourceId and mv.metric.metricId=:metricId", entityClass)
            .setParameter("resourceId", resourceId)
            .setParameter("metricId", metricId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a metric value by its resource and metric.
     *
     * @param sessionManager the database session manager
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a Maybe that emits the metric value if it exists, else null
     */
    public Maybe<MetricValue> findByResourceAndMetricAndFetch(SessionManager sessionManager, long resourceId,
            long metricId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select mv from Resource r " +
                "left join r.metricValues mv " +
                "left join fetch mv.metric m " +
                "left join fetch m.metricType mt " +
                "where r.resourceId=:resourceId and mv.metric.metricId=:metricId", entityClass)
            .setParameter("resourceId", resourceId)
            .setParameter("metricId", metricId)
            .getSingleResultOrNull()
        );
    }
}
