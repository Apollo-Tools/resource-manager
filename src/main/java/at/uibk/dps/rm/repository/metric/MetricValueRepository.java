package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class MetricValueRepository extends Repository<MetricValue> {
    public MetricValueRepository(Stage.SessionFactory sessionFactory, Class<MetricValue> entityClass) {
        super(sessionFactory, entityClass);
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
            session.createQuery("from MetricValue mv left join fetch mv.metric "
                    + "where mv.resource.resourceId=:resourceId", entityClass)
                .setParameter("resourceId", resourceId)
                .getResultList()
        );
    }

    public CompletionStage<List<Resource>> findByMultipleMetricsAndFetch(List<String> metrics) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct r from Resource r "
                    + "left join fetch r.metricValues "
                    + "left join fetch r.resourceType "
                    + "where r.resourceId in ("
                    + "select mv.resource.resourceId from MetricValue mv "
                    + "where mv.metric.metric in :metrics "
                    + "group by mv.resource.resourceId "
                    + "having count(mv.metric.metric) = :metricAmount)", Resource.class)
                .setParameter("metrics", metrics)
                .setParameter("metricAmount", (long) metrics.size())
                .getResultList()
        );
    }

    public CompletionStage<MetricValue> findByResourceAndMetric(long resourceId, long metricId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from MetricValue mv "
                    + "where mv.resource.resourceId=:resourceId and mv.metric.metricId=:metricId",
                    entityClass)
                .setParameter("resourceId", resourceId)
                .setParameter("metricId", metricId)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<Integer> deleteByResourceAndMetric(long resourceId, long metricId) {
        return this.sessionFactory.withTransaction(session ->
            session.createQuery("delete from MetricValue mv "
                    + "where mv.resource.resourceId=:resourceId and mv.metric.metricId=:metricId")
                .setParameter("resourceId", resourceId)
                .setParameter("metricId", metricId)
                .executeUpdate()
        );
    }
}
