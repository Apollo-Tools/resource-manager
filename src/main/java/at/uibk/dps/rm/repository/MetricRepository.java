package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Metric;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class MetricRepository extends Repository<Metric> {

    public MetricRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Metric.class);
    }

    public CompletionStage<Metric> findByMetric(String metric) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Metric m where m.metric=:metric", entityClass)
                .setParameter("metric", metric)
                .getSingleResultOrNull()
        );
    }
}
