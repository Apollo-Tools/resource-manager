package at.uibk.dps.rm.handler.service;

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

    /**
     * Create an instance from the serviceChecker.
     *
     * @param serviceChecker the service checker
     */
    public ServiceHandler(ServiceChecker serviceChecker) {
        super(serviceChecker);
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return entityChecker.submitCreate(requestBody);
    }

    @Override
    protected Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> entityChecker.submitUpdate(id, requestBody));
    }
}
