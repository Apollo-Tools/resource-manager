package at.uibk.dps.rm.service.database.artifact;

import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.repository.artifact.ServiceTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.util.RxVertxHandler;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

/**
 * This is the implementation of the {@link ServiceTypeService}.
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
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        ServiceType serviceType = data.mapTo(ServiceType.class);
        Single<ServiceType> save = withTransactionSingle(sessionManager -> repository
            .findByName(sessionManager, serviceType.getName())
            .flatMap(existingType -> Maybe.<ServiceType>error(new AlreadyExistsException(ServiceType.class)))
            .switchIfEmpty(sessionManager.persist(serviceType))
        );
        RxVertxHandler.handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }
}
