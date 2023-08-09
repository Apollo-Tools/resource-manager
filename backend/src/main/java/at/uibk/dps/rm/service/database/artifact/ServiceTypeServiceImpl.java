package at.uibk.dps.rm.service.database.artifact;

import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.repository.artifact.ServiceTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #ServiceTypeService.
 *
 * @author matthi-g
 */
public class ServiceTypeServiceImpl extends DatabaseServiceProxy<ServiceType> implements ServiceTypeService {

    private final ServiceTypeRepository repository;
    /**
     * Create an instance from the repository.
     *
     * @param repository  the repository
     */
    public ServiceTypeServiceImpl(ServiceTypeRepository repository, SessionFactory sessionFactory) {
        super(repository, ServiceType.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        ServiceType serviceType = data.mapTo(ServiceType.class);
        CompletionStage<ServiceType> save = withTransaction(session ->
            repository.findByName(session, serviceType.getName())
                .thenCompose(existing -> {
                    ServiceResultValidator.checkExists(existing, ServiceType.class);
                    return session.persist(serviceType);
                })
                .thenApply(res -> serviceType));
        return sessionToFuture(save).map(JsonObject::mapFrom);
    }
}
