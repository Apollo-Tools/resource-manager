package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the service entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
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

    /**
     * Find all services that are accessible by the account.
     *
     * @param accountId the id of the account
     * @return a Single that emits the list of found services as JsonArray
     */
    public Single<JsonArray> checkFindAllAccessible(long accountId) {
        return ErrorHandler.handleFindAll(service.findAllAccessibleServices(accountId));
    }
}
