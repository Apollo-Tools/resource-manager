package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.util.validation.EntityNameValidator;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ServiceInputHandler {

    public static void validateAddServiceRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        String serviceName = requestBody.getString("name");
        EntityNameValidator.checkName(serviceName, Service.class)
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }
}
