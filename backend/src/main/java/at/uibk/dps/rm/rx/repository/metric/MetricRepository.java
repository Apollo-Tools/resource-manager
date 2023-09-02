package at.uibk.dps.rm.rx.repository.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;
/**
 * Implements database operations for the metric entity.
 *
 * @author matthi-g
 */
public class MetricRepository extends Repository<Metric> {

    /**
     * Create an instance.
     */
    public MetricRepository() {
        super(Metric.class);
    }

    /**
     * Find all metrics by their platform and if they are required or optional.
     *
     * @param sessionManager the database session manager
     * @param platformId the id of the platform
     * @param required whether the metrics are required or optional
     * @return a Single that emits a list of all metrics
     */
    public Single<List<Metric>> findAllByPlatformId(SessionManager sessionManager, long platformId, boolean required) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct m from PlatformMetric pm " +
                "left join pm.metric m " +
                "where pm.platform.platformId=:platformId and pm.required=:required", entityClass)
            .setParameter("platformId", platformId)
            .setParameter("required", required)
            .getResultList()
        );
    }

    /**
     * Find a metric by its name.
     *
     * @param sessionManager the database session manager
     * @param metric the name of the metric
     * @return a CompletionStage that emits the metric if it exists, else null
     */
    public Maybe<Metric> findByMetric(SessionManager sessionManager, String metric) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Metric m where m.metric=:metric",
                entityClass)
            .setParameter("metric", metric)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a slo metric by its name.
     *
     * @param sessionManager the database session manager
     * @param metric the name of the metric
     * @return a CompletionStage that emits the metric if it exists, else null
     */
    public Maybe<Metric> findByMetricAndIsSLO(SessionManager sessionManager, String metric) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Metric m where m.metric=:metric and m" +
                    ".isSlo=true"
                , entityClass)
            .setParameter("metric", metric)
            .getSingleResultOrNull()
        );
    }
}
