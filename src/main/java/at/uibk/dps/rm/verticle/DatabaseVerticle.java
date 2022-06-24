package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.repository.resource.entity.ResourceType;
import at.uibk.dps.rm.service.resource.ResourceService;
import at.uibk.dps.rm.service.resource.ResourceServiceImpl;
import at.uibk.dps.rm.service.resource.ResourceTypeService;
import at.uibk.dps.rm.service.resource.ResourceTypeServiceImpl;
import io.reactivex.rxjava3.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import javax.persistence.Persistence;
import java.util.Map;

public class DatabaseVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);

    private SessionFactory sessionFactory;

    private ResourceTypeRepository resourceTypeRepository;
    private ResourceRepository resourceRepository;

    @Override
    public Completable rxStart() {
        return setupDatabase()
                .andThen(initializeRepositories())
                .andThen(setupEventBus());
    }

    private Completable setupDatabase() {
        Maybe<Void> startDB = vertx.rxExecuteBlocking(emitter -> {
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
            logger.info("Connection to database established");
            emitter.complete();
        });
        return Completable.fromMaybe(startDB);
    }

    private Completable initializeRepositories() {
        Maybe<Void> setupServices = Maybe.create(emitter -> {
                resourceTypeRepository = new ResourceTypeRepository(sessionFactory, ResourceType.class);
                resourceRepository = new ResourceRepository(sessionFactory, Resource.class);
                emitter.onComplete();
            }
        );
        return Completable.fromMaybe(setupServices);
    }

    private Completable setupEventBus() {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            ResourceTypeService resourceTypeService =
                new ResourceTypeServiceImpl(resourceTypeRepository);
            new ServiceBinder(vertx.getDelegate())
                .setAddress("resource-type-service-address")
                .register(ResourceTypeService.class, resourceTypeService);

            ResourceService resourceService =
                new ResourceServiceImpl(resourceRepository);
            new ServiceBinder(vertx.getDelegate())
                .setAddress("resource-service-address")
                .register(ResourceService.class, resourceService);
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
