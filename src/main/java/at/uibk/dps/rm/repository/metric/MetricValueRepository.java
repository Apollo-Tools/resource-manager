package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class MetricValueRepository extends Repository<MetricValue> {
    public MetricValueRepository(Stage.SessionFactory sessionFactory, Class<MetricValue> entityClass) {
        super(sessionFactory, entityClass);
    }

    public CompletionStage<Void> createAll(List<MetricValue> metricValueList) {
        return sessionFactory.withTransaction((session, tx) ->
            session.persist(metricValueList.toArray())
        );
    }

    public CompletionStage<MetricValue> findByResourceAndMetric(Long resourceId, Long metricId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from MetricValue mv "
                    + "where mv.resource.resourceId=:resourceId and mv.metric.metricId=:metricId",
                    entityClass)
                .setParameter("resourceId", resourceId)
                .setParameter("metricId", metricId)
                .getSingleResultOrNull()
        );
    }
}
