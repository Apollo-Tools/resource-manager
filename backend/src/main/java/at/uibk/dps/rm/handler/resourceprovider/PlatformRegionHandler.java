package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the regions linked to the platform entity.
 *
 * @author matthi-g
 */
public class PlatformRegionHandler extends ValidationHandler {

    private final RegionService regionService;

    /**
     * Create an instance from the regionService.
     *
     * @param regionService the service
     */
    public PlatformRegionHandler(RegionService regionService) {
        super(regionService);
        this.regionService = regionService;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(regionService::findAllByPlatformId);
    }
}
