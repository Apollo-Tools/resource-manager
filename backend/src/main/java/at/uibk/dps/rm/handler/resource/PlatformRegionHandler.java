package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the regions linked to the platform entity.
 *
 * @author matthi-g
 */
@Deprecated
public class PlatformRegionHandler extends ValidationHandler {

    private final PlatformChecker platformChecker;

    private final RegionChecker regionChecker;

    /**
     * Create an instance from the platformChecker.
     *
     * @param platformChecker the platform checker
     */
    public PlatformRegionHandler(PlatformChecker platformChecker, RegionChecker regionChecker) {
        super(platformChecker);
        this.platformChecker = platformChecker;
        this.regionChecker = regionChecker;
    }

    //TODO: move into service
    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> platformChecker.checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMap(regionChecker::checkFindAllByPlatform);
    }
}
