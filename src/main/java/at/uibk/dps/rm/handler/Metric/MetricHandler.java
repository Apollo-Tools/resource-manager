package at.uibk.dps.rm.handler.Metric;

import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.service.metric.MetricService;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class MetricHandler extends RequestHandler {

    private final MetricService metricService;

    public MetricHandler(Vertx vertx) {
        super(MetricService.createProxy(vertx.getDelegate(),"metric-service-address"));
        metricService = (MetricService) service;
    }

    @Override
    public void post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        checkForDuplicateMetric(rc, requestBody.getString("metric"))
            .onComplete(existsHandler -> {
                if (!rc.failed()) {
                    submitCreate(rc, requestBody);
                }
            });
    }

    @Override
    public void patch(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(id -> checkUpdateExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    private Future<Boolean> checkForDuplicateMetric(RoutingContext rc, String metric) {
        return metricService.existsOneByMetric(metric)
            .onComplete(duplicateHandler -> ErrorHandler.handleDuplicates(rc, duplicateHandler));
    }

    private void checkUpdateExists(RoutingContext rc, long id) {
        checkFindOne(rc, id)
            .onComplete(updateHandler -> {
                if (!rc.failed()) {
                    checkUpdateNoDuplicate(rc, updateHandler.result());
                }
            });
    }

    private void checkUpdateNoDuplicate(RoutingContext rc, JsonObject entity) {
        JsonObject requestBody = rc.body().asJsonObject();
        if (requestBody.containsKey("metric")) {
            checkForDuplicateMetric(rc, requestBody.getString("metric"))
                .onComplete(duplicateHandler -> {
                    if (!rc.failed()) {
                        submitUpdate(rc, requestBody, entity);
                    }
                });
        } else {
            submitUpdate(rc, requestBody, entity);
        }
    }
}
