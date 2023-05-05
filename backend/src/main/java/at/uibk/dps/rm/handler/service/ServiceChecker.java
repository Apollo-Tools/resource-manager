package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class ServiceChecker extends EntityChecker {

    private final ServiceService service;
    /**
     * Create an instance from the service service.
     *
     * @param service the service to use
     */
    public ServiceChecker(ServiceService service) {
        super(service);
        this.service = service;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsByName = service.existsOneByName(entity.getString("name"));
        return ErrorHandler.handleDuplicates(existsByName).ignoreElement();
    }
}
