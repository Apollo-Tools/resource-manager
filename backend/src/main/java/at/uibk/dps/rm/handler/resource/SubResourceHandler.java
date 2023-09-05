package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the sub resource entity.
 *
 * @author matthi-g
 */
@Deprecated
public class SubResourceHandler  extends ValidationHandler {

    private final ResourceService resourceService;

    /**
     * Create an instance from the resourceChecker.
     *
     * @param resourceService the resource checker
     */
    public SubResourceHandler(ResourceService resourceService) {
        super(resourceService);
        this.resourceService = resourceService;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(resourceService::findAllSubResources);
    }
}
