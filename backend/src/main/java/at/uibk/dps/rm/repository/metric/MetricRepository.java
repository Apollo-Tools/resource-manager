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

    public CompletionStage<List<Metric>> findAllByResourceTypeId(long resourceTypeId, boolean required) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct m from ResourceTypeMetric rtm " +
                "left join rtm.metric m " +
                "where rtm.resourceType.typeId=:resourceTypeId and rtm.required=:required", entityClass)
                .setParameter("resourceTypeId", resourceTypeId)
                .setParameter("required", required)
                .getResultList()
        );
    }

    public CompletionStage<Metric> findByMetric(String metric) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Metric m where m.metric=:metric", entityClass)
                .setParameter("metric", metric)
                .getSingleResultOrNull()
        );
    }
}
