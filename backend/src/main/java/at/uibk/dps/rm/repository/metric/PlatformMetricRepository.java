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

    /**
     * Find all platform metrics by their platform
     *
     * @param session the database session
     * @param platformId the id of the platform
     * @return a CompletionStage that emits all platform metrics
     */
    public CompletionStage<List<PlatformMetric>> findAllByPlatform(Session session, long platformId) {
        return session.createQuery("from PlatformMetric pm " +
                "left join fetch pm.metric m " +
                "left join fetch  m.metricType " +
                "where pm.platform.platformId=:platformId", entityClass)
            .setParameter("platformId", platformId)
            .getResultList();
    }

    /**
     * Find a platform metric by its platform and metric
     *
     * @param session the database session
     * @param platformId the id of the platform
     * @param metricId the id of the metric
     * @return a CompletionStage that emits the platform metric if it exists else null
     */
    public CompletionStage<PlatformMetric> findByPlatformAndMetric(Session session, long platformId, long metricId) {
        return session.createQuery("from PlatformMetric pm " +
                "left join fetch pm.metric m " +
                "left join fetch m.metricType " +
                "where pm.platform.platformId=:platformId and m.metricId=:metricId", entityClass)
            .setParameter("platformId", platformId)
            .setParameter("metricId", metricId)
            .getSingleResultOrNull();
    }

    /**
     * Find a platform metric by resource and metric
     *
     * @param session the database session
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a CompletionStage that emits the platform metric if it exists else null
     */
    public CompletionStage<PlatformMetric> findByResourceAndMetric(Session session, long resourceId, long metricId) {
        return session.createQuery("select distinct pm from PlatformMetric pm " +
                "where (pm.platform.platformId=(" +
                    "select mr.platform.platformId from MainResource mr where mr.resourceId=:resourceId) " +
                "or pm.platform.platformId=(" +
                    "select sr.mainResource.platform.platformId from SubResource sr where sr.resourceId=:resourceId)) " +
                "and pm.metric.metricId=:metricId", entityClass)
            .setParameter("resourceId", resourceId)
            .setParameter("metricId", metricId)
            .getSingleResultOrNull();
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
