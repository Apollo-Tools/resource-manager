package at.uibk.dps.rm.rx.handler.metric;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.metric.PlatformMetricService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the resource_type_metric entity.
 *
 * @author matthi-g
 */
public class PlatformMetricHandler extends ValidationHandler {

    private final PlatformMetricService platformMetricService;

    /**
     * Create an instance from the platformMetricService.
     *
     * @param platformMetricService the service
     */
    public PlatformMetricHandler(PlatformMetricService platformMetricService) {
        super(platformMetricService);
        this.platformMetricService = platformMetricService;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(platformMetricService::findAllByPlatformId);
    }
}
