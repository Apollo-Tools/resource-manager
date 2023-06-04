package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the resource entity.
 *
 * @author matthi-g
 */
public class ResourceHandler extends ValidationHandler {

    private final PlatformChecker platformChecker;

    private final RegionChecker regionChecker;

    /**
     * Create an instance from the resourceChecker, platformChecker and regionChecker.
     *
     * @param resourceChecker the resource checker
     * @param platformChecker the platform checker
     * @param regionChecker the region checker
     */
    public ResourceHandler(ResourceChecker resourceChecker, PlatformChecker platformChecker, RegionChecker regionChecker) {
        super(resourceChecker);
        this.platformChecker = platformChecker;
        this.regionChecker = regionChecker;
    }

    // TODO: delete metric values on delete check if resource has metric values

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return platformChecker.checkExistsOne(requestBody.getJsonObject("platform").getLong("platform_id"))
            .andThen(regionChecker.checkExistsOne(requestBody.getJsonObject("region").getLong("region_id")))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }
}
