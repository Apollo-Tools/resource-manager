package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.util.validation.CollectionValidator;
import at.uibk.dps.rm.util.validation.EntityNameValidator;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

/**
 * Used to validate the inputs of the service endpoint and fails the context if violations are found.
 *
 * @author matthi-g
 */
@UtilityClass
public class ServiceInputHandler {
    /**
     * Validate an add service request.
     *
     * @param rc the routing context
     */
    public static void validateAddServiceRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        String serviceName = requestBody.getString("name");
        //noinspection unchecked
        EntityNameValidator.checkName(serviceName, Service.class)
            .andThen(CollectionValidator.hasDuplicates(requestBody.getJsonArray("ports").getList()))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    /**
     * Validate an update service request
     *
     * @param rc the routing context
     */
    public static void validateUpdateServiceRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        //noinspection unchecked
        CollectionValidator.hasDuplicates(requestBody.getJsonArray("ports").getList())
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }
}
