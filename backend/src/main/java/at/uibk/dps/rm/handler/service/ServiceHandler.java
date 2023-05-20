package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.ValidationHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the service entity.
 *
 * @author matthi-g
 */
public class ServiceHandler extends ValidationHandler {

    private final ServiceTypeChecker serviceTypeChecker;
    /**
     * Create an instance from the serviceChecker and serviceTypeChecker.
     *
     * @param serviceChecker the service checker
     * @param serviceTypeChecker the service type checker
     */
    public ServiceHandler(ServiceChecker serviceChecker, ServiceTypeChecker serviceTypeChecker) {
        super(serviceChecker);
        this.serviceTypeChecker = serviceTypeChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return serviceTypeChecker.checkExistsOne(requestBody
                .getJsonObject("service_type")
                .getLong("service_type_id"))
            .andThen(entityChecker.checkForDuplicateEntity(requestBody))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }
}
