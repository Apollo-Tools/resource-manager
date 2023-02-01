package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceProviderRegionHandler extends ValidationHandler {
    private final RegionChecker regionChecker;

    public ResourceProviderRegionHandler(RegionService regionService) {
        super(new RegionChecker(regionService));
        regionChecker = (RegionChecker) super.entityChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(regionChecker::checkFindAllByProvider);
    }
}
