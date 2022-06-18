package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.metric.entity.Metric;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.repository.resource.entity.ResourceType;
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
    private Repository<Resource> resourceRepository;

    private Repository<ResourceType> resourceTypeRepository;

    private Repository<Metric> metricRepository;

    @Override
    public Uni<Void> asyncStart() {
        return setupDatabase()
                .chain(this::initializeDBServices)
                .chain(this::setupEventBus);
    }

    private Uni<Void> setupDatabase() {
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
                .onItem().invoke(() -> logger.info("Connection to database established"));

        return Uni.combine().all().unis(startHibernate).discardItems();
    }

    private Uni<Void> initializeDBServices() {
        Uni<Repository<Resource>> initResourceRepository = Uni.createFrom()
                .item(new Repository<>(sessionFactory, Resource.class))
                .onItem()
                .invoke(repo -> resourceRepository = repo);
        Uni<Repository<ResourceType>> initResourceTypeRepository = Uni.createFrom()
                .item(new Repository<>(sessionFactory, ResourceType.class))
                .onItem()
                .invoke(repo -> resourceTypeRepository = repo);

        Uni<Repository<Metric>> initMetricRepository = Uni.createFrom()
                .item(new Repository<>(sessionFactory, Metric.class))
                .onItem()
                .invoke(repo -> metricRepository = repo);

        return Uni.combine().all()
                .unis(initResourceRepository,
                        initResourceTypeRepository,
                        initMetricRepository)
                .discardItems();
    }

    private Uni<Void> setupEventBus() {
        EventBus eb = vertx.eventBus();
        MessageConsumer<String> consumer = eb.consumer("insert-metric");
        consumer.handler(message -> {
            ResourceType resourceType = new ResourceType();
            resourceType.setResource_type("test");

            // create
            resourceTypeRepository.create(resourceType)
                    .subscribe()
                    .with(result-> logger.info("Persisted: " + result),
                            throwable -> logger.error("Persisting failed", throwable));

            // read
            resourceTypeRepository.findById(1)
                    .subscribe()
                    .with(result-> logger.info("Found: " + result),
                    throwable -> logger.error("Retrieval failed", throwable));

            resourceTypeRepository.findAll()
                    .subscribe()
                    .with(result-> logger.info("Found: " + result.size()),
                            throwable -> logger.error("Retrieval failed", throwable));

            // update - only works by itself
            /*
            resourceTypeRepository.findById(1)
                    .subscribe()
                    .with(result-> {
                        if(result == null) {
                            logger.error("Entity not found", new NoSuchElementException());
                            return;
                        }
                        result.setResource_type("newtype");
                        resourceTypeRepository.update(result)
                                .subscribe()
                                .with(updated-> logger.info("Updated: " + updated),
                                        throwable -> logger.error("Updating failed", throwable));
                        },
                        throwable -> logger.error("Retrieval failed", throwable));
            // delete - only works by itself
            resourceTypeRepository.delete(25)
                    .subscribe()
                    .with(result-> logger.info("Deleted entity with id: " + 25),
                            throwable -> logger.error("Deletion failed", throwable));
             */
        });

        return Uni.createFrom().voidItem();
    }
}
