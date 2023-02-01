package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class MetricValueRepository extends Repository<MetricValue> {
    public MetricValueRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, MetricValue.class);
    }

    public CompletionStage<MetricValue> findByIdAndFetch(long metricValueId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from MetricValue  mv left join fetch mv.metric "
                    + "where mv.metricValueId = :metricValueId", entityClass)
                .setParameter("metricValueId", metricValueId)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<MetricValue>> findByResourceAndFetch(long resourceId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select mv from Resource r " +
                    "left join r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "where r.resourceId=:resourceId", entityClass)
                .setParameter("resourceId", resourceId)
                .getResultList()
        );
    }

    public CompletionStage<MetricValue> findByResourceAndMetric(long resourceId, long metricId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select mv from Resource r " +
                        "left join r.metricValues mv " +
                        "where r.resourceId=:resourceId and mv.metric.metricId=:metricId",
                    entityClass)
                .setParameter("resourceId", resourceId)
                .setParameter("metricId", metricId)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<Void> updateByResourceAndMetric(long resourceId, long metricId, String valueString,
                                                              Double valueNumber, Boolean valueBool) {
        return this.sessionFactory.withTransaction(session ->
                session.createQuery("select mv from Resource r " +
                        "left join r.metricValues mv " +
                        "where r.resourceId=:resourceId and mv.metric.metricId=:metricId", entityClass)
                        .setParameter("resourceId", resourceId)
                        .setParameter("metricId", metricId)
                        .getResultList()
                        .thenAccept(metricValues -> metricValues.forEach(metricValue -> {
                            metricValue.setValueString(valueString);
                            if (valueNumber!= null) {
                                metricValue.setValueNumber(valueNumber);
                            }
                            metricValue.setValueBool(valueBool);
                        })));
    }

    public CompletionStage<Integer> deleteByResourceAndMetric(long resourceId, long metricId) {
        return this.sessionFactory.withTransaction(session ->
            session.createQuery("select mv.metricValueId from Resource r " +
                    "left join r.metricValues mv " +
                    "where r.resourceId=:resourceId and mv.metric.metricId=:metricId", Long.class)
                .setParameter("resourceId", resourceId)
                .setParameter("metricId", metricId)
                .getSingleResult()
                .thenCompose(result -> session.createQuery("delete from MetricValue mv " +
                    "where mv.metricValueId=:metricValue")
                    .setParameter("metricValue", result)
                    .executeUpdate())
        );
    }
}
