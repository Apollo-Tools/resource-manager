package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern regions that are linked to the resource_provider entity.
 *
 * @author matthi-g
 */
public class ResourceProviderRegionHandler extends ValidationHandler {
    private final RegionChecker regionChecker;

    /**
     * Create an instance from the regionChecker.
     *
     * @param regionChecker the region checker
     */
    public ResourceProviderRegionHandler(RegionChecker regionChecker) {
        super(regionChecker);
        this.regionChecker = regionChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(regionChecker::checkFindAllByProvider);
    }
}
