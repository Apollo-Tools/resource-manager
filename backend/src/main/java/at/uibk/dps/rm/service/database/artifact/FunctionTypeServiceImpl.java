package at.uibk.dps.rm.service.database.artifact;

import at.uibk.dps.rm.entity.model.FunctionType;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.repository.artifact.FunctionTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * This is the implementation of the {@link FunctionTypeService}.
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
    public FunctionTypeServiceImpl(FunctionTypeRepository repository, SessionManagerProvider smProvider) {
        super(repository, FunctionType.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        FunctionType functionType = data.mapTo(FunctionType.class);
        Single<FunctionType> save = smProvider.withTransactionSingle(sm -> repository
            .findByName(sm, functionType.getName())
            .flatMap(existingType -> Maybe.<FunctionType>error(new AlreadyExistsException(FunctionType.class)))
            .switchIfEmpty(sm.persist(functionType)));
        RxVertxHandler.handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }
}
