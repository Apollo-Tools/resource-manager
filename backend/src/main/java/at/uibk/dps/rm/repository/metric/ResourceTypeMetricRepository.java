package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.ResourceTypeMetric;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class ResourceTypeMetricRepository extends Repository<ResourceTypeMetric> {
    public ResourceTypeMetricRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceTypeMetric.class);
    }

    public CompletionStage<Long> countMissingRequiredMetricValuesByResourceId(long resourceId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select count(rt) from ResourceTypeMetric rt " +
                "where rt.resourceType.typeId in (" +
                "       select r.resourceType.typeId from Resource r where r.resourceId=:resourceId " +
                "   ) and rt.required=true " +
                    "and metric.metricId not in ( " +
                "       select mv.metric.metricId from Resource r left join r.metricValues mv where r.resourceId=:resourceId" +
                "   )", Long.class)
                .setParameter("resourceId", resourceId)
                .getSingleResult()
        );
    }
}