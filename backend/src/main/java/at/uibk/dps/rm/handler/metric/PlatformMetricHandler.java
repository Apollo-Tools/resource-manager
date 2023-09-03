package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the resource_type_metric entity.
 *
 * @author matthi-g
 */
@Deprecated
public class PlatformMetricHandler extends ValidationHandler {

    private final PlatformMetricChecker platformMetricChecker;

    /**
     * Create an instance from the metricChecker and resourceTypeChecker.
     *
     * @param platformMetricChecker the platform metric checker
     */
    public PlatformMetricHandler(PlatformMetricChecker platformMetricChecker) {
        super(platformMetricChecker);
        this.platformMetricChecker = platformMetricChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(platformMetricChecker::checkFindAllByPlatformId);
    }
}
