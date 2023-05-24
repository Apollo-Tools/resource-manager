package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.*;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the resource entity.
 *
 * @author matthi-g
 */
public class ResourceHandler extends ValidationHandler {

    private final ResourceTypeChecker resourceTypeChecker;

    /**
     * Create an instance from the resourceChecker and resourceTypeChecker
     *
     * @param resourceChecker the resource checker
     * @param resourceTypeChecker the resource type checker
     */
    public ResourceHandler(ResourceChecker resourceChecker, ResourceTypeChecker resourceTypeChecker) {
        super(resourceChecker);
        this.resourceTypeChecker = resourceTypeChecker;
    }

    // TODO: delete metric values on delete check if resource has metric values

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return resourceTypeChecker.checkExistsOne(requestBody
                .getJsonObject("resource_type")
                .getLong("type_id"))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }
}
