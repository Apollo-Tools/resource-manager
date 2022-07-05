package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.metric.entity.Metric;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class MetricRepository extends Repository<Metric> {

    public MetricRepository(Stage.SessionFactory sessionFactory,
        Class<Metric> entityClass) {
        super(sessionFactory, entityClass);
    }

    public CompletionStage<Metric> findByMetric(String metric) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Metric m where m.metric=:metric", entityClass)
                .setParameter("metric", metric)
                .getSingleResultOrNull()
        );
    }
}
