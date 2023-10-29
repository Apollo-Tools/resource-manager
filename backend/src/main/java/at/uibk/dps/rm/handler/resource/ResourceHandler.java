package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the resource entity.
 *
 * @author matthi-g
 */
public class ResourceHandler extends ValidationHandler {

    private final ResourceService resourceService;

    /**
     * Create an instance from the resourceService.
     *
     * @param resourceService the service
     */
    public ResourceHandler(ResourceService resourceService) {
        super(resourceService);
        this.resourceService = resourceService;
    }

    /**
     * Find all sub resources of a main resource.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits all found sub resources as JsonArray
     */
    public Single<JsonArray> getAllSubResourcesByMainResource(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(resourceService::findAllSubResources);
    }

    /**
     * Find and return all resources that fulfill the service level objectives from the request.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entities as JsonArray
     */
    public Single<JsonArray> getAllBySLOs(RoutingContext rc) {
        return resourceService.findAllBySLOs(rc.body().asJsonObject());
    }

    /**
     * Saves all resources given in TOSCA format
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    @Override
    protected Completable postAll(RoutingContext rc) {
        return resourceService.saveStandardized(rc.body().asString());
    }

    public Single<JsonObject> getStandarizedResource(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
                .flatMap(resourceService::getStandardized);
    }

    public Single<JsonObject> getAllStandarizedResource() {
        return resourceService.getAllStandardized();
    }
}
