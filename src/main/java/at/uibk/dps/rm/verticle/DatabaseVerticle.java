package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.service.database.DatabaseService;
import at.uibk.dps.rm.service.database.DatabaseServiceImpl;
import at.uibk.dps.rm.service.resource.ResourceTypeService;
import at.uibk.dps.rm.service.resource.ResourceTypeServiceImpl;
import io.reactivex.rxjava3.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class DatabaseVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);

    private DatabaseService databaseService;

    private ResourceTypeService resourceTypeService;

    @Override
    public Completable rxStart() {
        return setupDatabase()
                .andThen(setupEventBus());
    }

    private Completable setupDatabase() {
        Maybe<Void> startDB = vertx.rxExecuteBlocking(block -> {
            databaseService = new DatabaseServiceImpl(config());
            new ServiceBinder(vertx.getDelegate())
                    .setAddress("database-service-address")
                    .register(DatabaseService.class, databaseService);
            logger.info("Connection to database established");
            block.complete();
        });
        return Completable.fromMaybe(startDB);
    }

    private Completable setupEventBus() {
        Observable<Object> setupEventBus = Observable.create(emitter -> {
            resourceTypeService = new ResourceTypeServiceImpl(vertx.getDelegate());
            new ServiceBinder(vertx.getDelegate())
                    .setAddress("resource-type-service-address")
                    .register(ResourceTypeService.class, resourceTypeService);
        /*
            MessageConsumer<String> consumer = eb.consumer("insert-metric");
            consumer.handler(message -> {
                ResourceType resourceType = new ResourceType();
                resourceType.setResource_type("test");

                // create
                this.resourceTypeRepository.create(resourceType)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Persisting failed", throwable);
                        } else {
                            logger.info("Persisted: " + result);
                        }
                    });

                // read
                resourceTypeRepository.findById(1)
                        .whenComplete((result, throwable) ->
                        {
                            if (throwable != null) {
                                logger.error("Retrieval failed", throwable);
                            } else {
                                logger.info("Found: " + result);
                            }
                        });

                resourceTypeRepository.findAll()
                        .whenComplete((result, throwable) ->
                        {
                            if (throwable != null) {
                                logger.error("Retrieval failed", throwable);
                            } else {
                                logger.info("Found: " + result.size());
                            }
                        });
                /* update - only works by itself
                resourceTypeRepository.findById(1)
                        .whenComplete((result, throwable) -> {
                            if(throwable != null) {
                                logger.error("Retrieval failed", throwable);
                            }
                            if(result == null) {
                                logger.error("Entity not found", new NoSuchElementException());
                                return;
                            }
                            result.setResource_type("newtype");
                            resourceTypeRepository.update(result)
                                .whenComplete((updated, throwableUpdate) -> {
                                    if(throwable != null) {
                                        logger.error("Updating failed", throwable);
                                    } else {
                                        logger.info("Updated: " + updated);
                                    }
                                });
                            });
                 */
                /* delete - only works by itself
                resourceTypeRepository.delete(4)
                        .whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                logger.error("Deletion failed", throwable);
                            } else {
                                logger.info("Deleted entity with id: " + 4);
                            }
                        });

            });
            */
            emitter.onComplete();
        });
        return Completable.fromObservable(setupEventBus);
    }
}
