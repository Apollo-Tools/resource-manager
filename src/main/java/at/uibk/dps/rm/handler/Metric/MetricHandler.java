package at.uibk.dps.rm.handler.Metric;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.service.rxjava3.metric.MetricService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class MetricHandler extends RequestHandler {

    private final MetricService metricService;

    public MetricHandler(Vertx vertx) {
        super(MetricService.createProxy(vertx,"metric-service-address"));
        metricService = (MetricService) service;
    }

    @Override
    public Disposable post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return checkForDuplicateMetric(requestBody.getString("metric"))
            .andThen(submitCreate(requestBody))
            .subscribe(result -> ResultHandler.handleSaveRequest(rc, result),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    @Override
    public Disposable patch(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(super::checkFindOne)
            .flatMap(result -> checkUpdateNoDuplicate(requestBody, result))
            .map(result -> submitUpdate(requestBody, result))
            .subscribe(result -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    private Completable checkForDuplicateMetric(String metric) {
        Single<Boolean> existsOneByMetric = metricService.existsOneByMetric(metric);
        return ErrorHandler.handleDuplicates(existsOneByMetric).ignoreElement();
    }

    private Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("metric")) {
            return checkForDuplicateMetric(requestBody.getString("metric"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }
}
