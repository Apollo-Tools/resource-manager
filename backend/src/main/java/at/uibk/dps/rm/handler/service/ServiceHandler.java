package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the service entity.
 *
 * @author matthi-g
 */
public class ServiceHandler extends ValidationHandler {

    private final ServiceService serviceService;
    /**
     * Create an instance from the serviceService.
     *
     * @param serviceService the service
     */
    public ServiceHandler(ServiceService serviceService) {
        super(serviceService);
        this.serviceService = serviceService;
    }

    @Override
    public Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return serviceService.findAllAccessibleServices(accountId);
    }

    @Override
    public Single<JsonArray> getAllFromAccount(RoutingContext rc) {
        return super.getAllFromAccount(rc);
    }
}
