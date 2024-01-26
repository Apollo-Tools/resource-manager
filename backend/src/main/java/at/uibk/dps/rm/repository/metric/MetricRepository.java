package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    public Single<List<Metric>> findAllNonMonitoredBySLO(SessionManager sessionManager,
            List<ServiceLevelObjective> serviceLevelObjectives) {
        String mappedSLOs = serviceLevelObjectives.stream()
            .map(ServiceLevelObjective::getName)
            .collect(Collectors.joining(","));
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct m from PlatformMetric pm " +
                "left join pm.metric m " +
                "where m.metric in (:slos) and m.isSlo=true and pm.isMonitored=false", entityClass)
            .setParameter("slos", mappedSLOs)
            .getResultList()
        );
    }

    public Single<List<Metric>> findAllBySLOs(SessionManager sessionManager,
            Collection<ServiceLevelObjective> serviceLevelObjectives) {
        String mappedSLOs = serviceLevelObjectives.stream()
            .map(ServiceLevelObjective::getName)
            .collect(Collectors.joining(","));
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select m from Metric m " +
                "where m.metric in (:slos) and m.isSlo=true", entityClass)
            .setParameter("slos", mappedSLOs)
            .getResultList()
        );
    }
}
