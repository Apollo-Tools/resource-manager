package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the platform_metric entity.
 *
 * @author matthi-g
 */
public class PlatformMetricRepository extends Repository<PlatformMetric> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public PlatformMetricRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, PlatformMetric.class);
    }

    /**
     * Count how many required metrics are not registered for a resource.
     *
     * @param resourceId the id of  the resource
     * @return a CompletionStage that emits the amount of missing required metrics
     */
    public CompletionStage<Long> countMissingRequiredMetricValuesByResourceId(long resourceId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select count(pm) from PlatformMetric pm " +
                "where pm.platform.platformId in (" +
                "       select r.platform.platformId from Resource r where r.resourceId=:resourceId " +
                "   ) and pm.required=true " +
                    "and metric.metricId not in ( " +
                "       select mv.metric.metricId from Resource r left join r.metricValues mv where r.resourceId=:resourceId" +
                "   )", Long.class)
                .setParameter("resourceId", resourceId)
                .getSingleResult()
        );
    }
}
