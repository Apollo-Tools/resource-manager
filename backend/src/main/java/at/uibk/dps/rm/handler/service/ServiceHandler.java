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

    /**
     * Create an instance from the serviceChecker.
     *
     * @param serviceChecker the service checker
     */
    public ServiceHandler(ServiceChecker serviceChecker) {
        super(serviceChecker);
    }

    @Override
    public Single<JsonArray> getAll(RoutingContext rc) {
        return super.getAll(rc);
    }

    @Override
    public Single<JsonArray> getAllFromAccount(RoutingContext rc) {
        return super.getAllFromAccount(rc);
    }
}
