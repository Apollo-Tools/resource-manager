package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the metric_value entity.
 *
 * @author matthi-g
 */
@Deprecated
public class MetricValueHandler extends ValidationHandler {

    private final MetricValueChecker metricValueChecker;

    /**
     * Create an instance from the metricValueChecker.
     *
     * @param metricValueChecker the metric value checker
     */
    public MetricValueHandler(MetricValueChecker metricValueChecker) {
        super(metricValueChecker);
        this.metricValueChecker = metricValueChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> metricValueChecker.checkFindAllByResource(id, true));
    }

    @Override
    public Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> metricValueChecker.submitCreateAll(id, requestBody));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMapCompletable(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .flatMapCompletable(metricId -> metricValueChecker.updateOneByValue(resourceId, metricId, requestBody))
            );
    }

    @Override
    public Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMapCompletable(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .flatMapCompletable(metricId -> metricValueChecker.submitDeleteMetricValue(resourceId, metricId))
            );
    }
}
