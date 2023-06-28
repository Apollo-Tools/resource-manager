package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
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
        return entityChecker.checkForDuplicateEntity(requestBody)
            .andThen(serviceTypeChecker.checkFindOne(requestBody.getJsonObject("service_type")
                .getLong("service_type_id"))
            )
            .flatMapCompletable(serviceType -> checkServiceTypePorts(serviceType,
                requestBody.getJsonArray("ports").size()))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }

    @Override
    protected Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> entityChecker.submitUpdate(id, requestBody));
    }

    private Completable checkServiceTypePorts(JsonObject serviceType, int portAmount) {
        String name = serviceType.getString("name");
        if (checkHasNoService(name, portAmount) || checkHasService(name, portAmount)) {
            return Completable.complete();
        } else {
            return Completable.error(new BadInputException("invalid ports for service selection"));
        }
    }

    private boolean checkHasNoService(String serviceType, int portAmount) {
        return serviceType.equals("NoService") && portAmount == 0;
    }

    private boolean checkHasService(String serviceType, int portAmount) {
        return !serviceType.equals("NoService") && portAmount > 0;
    }
}
