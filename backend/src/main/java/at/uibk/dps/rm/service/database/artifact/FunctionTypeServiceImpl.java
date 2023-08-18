package at.uibk.dps.rm.service.database.artifact;

import at.uibk.dps.rm.entity.model.FunctionType;
import at.uibk.dps.rm.repository.artifact.FunctionTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #FunctionTypeService.
 *
 * @author matthi-g
 */
public class FunctionTypeServiceImpl extends DatabaseServiceProxy<FunctionType> implements FunctionTypeService {

    private final FunctionTypeRepository repository;
    /**
     * Create an instance from the repository.
     *
     * @param repository  the repository
     */
    public FunctionTypeServiceImpl(FunctionTypeRepository repository, SessionFactory sessionFactory) {
        super(repository, FunctionType.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        FunctionType functionType = data.mapTo(FunctionType.class);
        CompletionStage<FunctionType> save = withTransaction(
                session -> repository.findByName(session, functionType.getName()).thenCompose(existing -> {
                    ServiceResultValidator.checkExists(existing, FunctionType.class);
                    return session.persist(functionType);
                }).thenApply(res -> functionType));
        return sessionToFuture(save).map(JsonObject::mapFrom);
    }
}
