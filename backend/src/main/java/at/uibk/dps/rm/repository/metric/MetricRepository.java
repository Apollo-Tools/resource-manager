package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the metric entity.
 *
 * @author matthi-g
 */
public class MetricRepository extends Repository<Metric> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public MetricRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Metric.class);
    }

    /**
     * Find all metrics by their platform and if they are required or optional.
     *
     * @param platformId the id of the platform
     * @param required whether the metrics are required or optional
     * @return a CompletionStage that emits a list of all metrics
     */
    public CompletionStage<List<Metric>> findAllByPlatformId(long platformId, boolean required) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct m from PlatformMetric pm " +
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
     * @param metric the name of the metric
     * @return a CompletionStage that emits the metric if it exists, else null
     */
    public CompletionStage<Metric> findByMetric(String metric) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Metric m where m.metric=:metric", entityClass)
                .setParameter("metric", metric)
                .getSingleResultOrNull()
        );
    }
}
