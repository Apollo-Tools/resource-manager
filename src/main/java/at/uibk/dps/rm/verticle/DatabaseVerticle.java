package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.metric.entity.Metric;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.MessageConsumer;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import javax.persistence.Persistence;
import java.util.Map;

public class DatabaseVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);
    private SessionFactory sessionFactory;

    @Override
    public Uni<Void> asyncStart() {
        Uni<Void> startHibernate = Uni.createFrom().deferred(() -> {
            int dbPort = config().getInteger("db_port");
            String dbHost = config().getString("db_host");
            String dbUser = config().getString("db_user");
            String dbPassword = config().getString("db_password");
            Map<String, String> props = Map.of(
                "javax.persistence.jdbc.url", "jdbc:postgresql://" + dbHost + ":" + dbPort + "/resource-manager",
                "javax.persistence.jdbc.user", dbUser,
                "javax.persistence.jdbc.password", dbPassword);

            sessionFactory = Persistence
                .createEntityManagerFactory("postgres-unit", props)
                .unwrap(SessionFactory.class);

            return Uni.createFrom().voidItem();
        });

        startHibernate = vertx.executeBlocking(startHibernate)
            .onItem().invoke(() -> logger.info("✅ Hibernate Reactive is ready"));

        EventBus eb = vertx.eventBus();

        MessageConsumer<String> consumer = eb.consumer("insert-metric");
        consumer.handler(message -> {

            // Example for persisting item
            Metric metric = new Metric();
            metric.setDescription("New Metric");
            metric.setMetric("Metric");

            sessionFactory.withTransaction( (session, tx) -> session
                .persist(metric)
                .replaceWith(metric)
                .onItem().invoke(result-> logger.info("Persisted: " + result.getMetric()))
                .onFailure().invoke(throwable -> logger.error("Persisting failed", throwable)))
                .subscribe().asCompletionStage();

            // Example for reading item
            sessionFactory.withSession(session -> session.find(Metric.class, 3)
                .onItem()
                .invoke(myEvent-> logger.info("Read: " + myEvent.getMetric_id()))
                .onFailure().invoke(throwable -> logger.error("Reading failed", throwable)))
                .subscribe().asCompletionStage();
        });

        return Uni.combine().all().unis(startHibernate).discardItems();
    }
}
