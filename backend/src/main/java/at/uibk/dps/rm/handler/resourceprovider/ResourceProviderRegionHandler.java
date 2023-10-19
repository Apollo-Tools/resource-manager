package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern regions that are linked to the resource_provider entity.
 *
 * @author matthi-g
 */
public class ResourceProviderRegionHandler extends ValidationHandler {
    private final RegionService regionService;

    /**
     * Create an instance from the regionService.
     *
     * @param regionService the service
     */
    public ResourceProviderRegionHandler(RegionService regionService) {
        super(regionService);
        this.regionService = regionService;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(regionService::findAllByProviderId);
    }
}
