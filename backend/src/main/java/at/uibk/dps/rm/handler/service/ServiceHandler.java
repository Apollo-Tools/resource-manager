package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.ValidationHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the service entity.
 *
 * @author matthi-g
 */
public class ServiceHandler extends ValidationHandler {

    private final ServiceChecker serviceChecker;
    /**
     * Create an instance from the serviceChecker.
     *
     * @param serviceChecker the service checker
     */
    public ServiceHandler(ServiceChecker serviceChecker) {
        super(serviceChecker);
        this.serviceChecker = serviceChecker;
    }

    @Override
    public Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return serviceChecker.checkFindAllAccessible(accountId);
    }

    @Override
    public Single<JsonArray> getAllFromAccount(RoutingContext rc) {
        return super.getAllFromAccount(rc);
    }
}
