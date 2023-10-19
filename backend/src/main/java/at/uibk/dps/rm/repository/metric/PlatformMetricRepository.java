package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

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
     * Find all platform metrics by their platform and fetch the metric and metric type.
     *
     * @param sessionManager the database session manager
     * @param platformId the id of the platform
     * @return a CompletionStage that emits all platform metrics
     */
    public Single<List<PlatformMetric>> findAllByPlatform(SessionManager sessionManager, long platformId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from PlatformMetric pm " +
                "left join fetch pm.metric m " +
                "left join fetch  m.metricType " +
                "where pm.platform.platformId=:platformId", entityClass)
            .setParameter("platformId", platformId)
            .getResultList()
        );
    }

    /**
     * Find a platform metric by its platform and metric and fetch the metric and metric type.
     *
     * @param sessionManager the database session manager
     * @param platformId the id of the platform
     * @param metricId the id of the metric
     * @return a Maybe that emits the platform metric if it exists else null
     */
    public Maybe<PlatformMetric> findByPlatformAndMetric(SessionManager sessionManager, long platformId,
                                                         long metricId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from PlatformMetric pm " +
                "left join fetch pm.metric m " +
                "left join fetch m.metricType " +
                "where pm.platform.platformId=:platformId and m.metricId=:metricId", entityClass)
            .setParameter("platformId", platformId)
            .setParameter("metricId", metricId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a platform metric by resource and metric.
     *
     * @param sessionManager the database session manager
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a Maybe that emits the platform metric if it exists else null
     */
    public Maybe<PlatformMetric> findByResourceAndMetric(SessionManager sessionManager, long resourceId, long metricId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct pm from PlatformMetric pm " +
                "where ((pm.platform.platformId=(" +
                    "select mr.platform.platformId from MainResource mr where mr.resourceId=:resourceId) and " +
                    "pm.isMainResourceMetric=true)" +
                "or (pm.platform.platformId=(" +
                    "select sr.mainResource.platform.platformId from SubResource sr where sr.resourceId=:resourceId) " +
                    "and pm.isSubResourceMetric=true)) " +
                "and pm.metric.metricId=:metricId", entityClass)
            .setParameter("resourceId", resourceId)
            .setParameter("metricId", metricId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Count how many required metrics are not registered for a resource.
     *
     * @param sessionManager the database session manager
     * @param resourceId the id of  the resource
     * @return a Single that emits the amount of missing required metrics
     */
    public Single<Long> countMissingRequiredMetricValuesByResourceId(SessionManager sessionManager, long resourceId,
            boolean isMainResource) {
        Single<Long> getMissingMetrics = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select count(pm) from PlatformMetric pm " +
                "where pm.platform.platformId in (" +
                "   select mr.platform.platformId from MainResource mr where mr.resourceId=:resourceId " +
                ") and pm.required=true and pm.isMainResourceMetric=true " +
                "and not exists ( " +
                "  select mv.metric.metricId from Resource r left join r.metricValues mv " +
                "  where r.resourceId=:resourceId and mv.metric.metricId=pm.metric.metricId" +
                ")", Long.class)
            .setParameter("resourceId", resourceId)
            .getSingleResult()
        );
        Single<Long> getMainMissingMetrics = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select count(pm) from PlatformMetric pm " +
                "where pm.platform.platformId in (" +
                "   select sr.mainResource.platform.platformId from SubResource sr where sr.resourceId=:resourceId " +
                ") and pm.required=true and pm.isMainResourceMetric=true " +
                "and not exists ( " +
                "  select mv.metric.metricId from SubResource sr left join sr.mainResource.metricValues mv " +
                "  where sr.resourceId=:resourceId and mv.metric.metricId=pm.metric.metricId" +
                ")", Long.class)
            .setParameter("resourceId", resourceId)
            .getSingleResult()
        );
        Single<Long> getSubMissingMetrics = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select count(pm) from PlatformMetric pm " +
                "where pm.platform.platformId in (" +
                "   select sr.mainResource.platform.platformId from SubResource sr where sr.resourceId=:resourceId " +
                ") and pm.required=true and pm.isSubResourceMetric=true " +
                "and not exists ( " +
                "  select mv.metric.metricId from Resource r left join r.metricValues mv " +
                "  where r.resourceId=:resourceId and mv.metric.metricId=pm.metric.metricId" +
                ")", Long.class)
            .setParameter("resourceId", resourceId)
            .getSingleResult()
        );
        if (isMainResource) {
            return getMissingMetrics;
        } else {
            return Single.zip(getMainMissingMetrics, getSubMissingMetrics, Long::sum);
        }
    }
}
