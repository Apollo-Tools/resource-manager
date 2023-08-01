package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the platform_metric entity.
 *
 * @author matthi-g
 */
public class PlatformMetricRepository extends Repository<PlatformMetric> {

    /**
     * Create an instance.
     */
    public PlatformMetricRepository() {
        super(PlatformMetric.class);
    }

    public CompletionStage<List<PlatformMetric>> findAllByPlatform(Session session, long platformId) {
        return session.createQuery("from PlatformMetric pm " +
                "left join fetch pm.metric m " +
                "left join fetch  m.metricType " +
                "where pm.platform.platformId=:platformId", entityClass)
            .setParameter("platformId", platformId)
            .getResultList();
    }

    /**
     * Count how many required metrics are not registered for a resource.
     *
     * @param session the database session
     * @param resourceId the id of  the resource
     * @return a CompletionStage that emits the amount of missing required metrics
     */
    public CompletionStage<Long> countMissingRequiredMetricValuesByResourceId(Session session, long resourceId,
            boolean isMainResource) {
        CompletionStage<Long> getMissingMetrics = session.createQuery("select count(pm) from PlatformMetric pm " +
                "where pm.platform.platformId in (" +
                "   select mr.platform.platformId from MainResource mr where mr.resourceId=:resourceId " +
                ") and pm.required=true and pm.isMainResourceMetric=true " +
                "and metric.metricId not in ( " +
                "  select mv.metric.metricId from Resource r left join r.metricValues mv where r.resourceId=:resourceId" +
                ")", Long.class)
            .setParameter("resourceId", resourceId)
            .getSingleResult();
        CompletionStage<Long> getMainMissingMetrics = session.createQuery("select count(pm) from PlatformMetric pm " +
                "where pm.platform.platformId in (" +
                "   select sr.mainResource.platform.platformId from SubResource sr where sr.resourceId=:resourceId " +
                ") and pm.required=true and pm.isMainResourceMetric=true " +
                "and metric.metricId not in ( " +
                "  select mv.metric.metricId from SubResource sr left join sr.mainResource.metricValues mv " +
                "  where sr.resourceId=:resourceId" +
                ")", Long.class)
            .setParameter("resourceId", resourceId)
            .getSingleResult();
        CompletionStage<Long> getSubMissingMetrics = session.createQuery("select count(pm) from PlatformMetric pm " +
                "where pm.platform.platformId in (" +
                "   select sr.mainResource.platform.platformId from SubResource sr where sr.resourceId=:resourceId " +
                ") and pm.required=true and pm.isSubResourceMetric=true " +
                "and metric.metricId not in ( " +
                "  select mv.metric.metricId from Resource r left join r.metricValues mv where r.resourceId=:resourceId" +
                ")", Long.class)
            .setParameter("resourceId", resourceId)
            .getSingleResult();
        if (isMainResource) {
            return getMissingMetrics;
        } else {
            return getMainMissingMetrics.thenCombine(getSubMissingMetrics, Long::sum);
        }
    }
}
