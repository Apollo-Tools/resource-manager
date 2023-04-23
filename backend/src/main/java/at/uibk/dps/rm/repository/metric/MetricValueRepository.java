package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the metric_value entity.
 *
 * @author matthi-g
 */
public class MetricValueRepository extends Repository<MetricValue> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public MetricValueRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, MetricValue.class);
    }

    /**
     * Find a metric value by its id and fetch the metric.
     *
     * @param metricValueId the id of the metric value
     * @return a CompletionStage that emits the metric value if it exists, else null
     */
    public CompletionStage<MetricValue> findByIdAndFetch(long metricValueId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from MetricValue  mv left join fetch mv.metric "
                    + "where mv.metricValueId = :metricValueId", entityClass)
                .setParameter("metricValueId", metricValueId)
                .getSingleResultOrNull()
        );
    }

    /**
     * Find all metric values by their resource.
     *
     * @param resourceId the id of the resource
     * @return a CompletionStage that emits a list of all metric values
     */
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

    /**
     * Find a metric value by its resource and metric.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a CompletionStage that emits the metric value if it exists, else null
     */
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

    /**
     * Update the values of a metric value by its resource and metric.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @param valueString the new string value
     * @param valueNumber the new number value
     * @param valueBool the new bool value
     * @return an empty CompletionStage
     */
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

    /**
     * Delete a metric value by its resource and metric
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a CompletionStage that emits the row count
     */
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
