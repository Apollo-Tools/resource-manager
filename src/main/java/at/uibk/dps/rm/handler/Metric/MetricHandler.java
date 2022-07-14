package at.uibk.dps.rm.handler.Metric;

import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.service.metric.MetricService;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class MetricHandler {

    private final MetricService metricService;

    public MetricHandler(Vertx vertx) {
        metricService = MetricService.createProxy(vertx.getDelegate(),
            "metric-service-address");
    }

    public void post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        checkForDuplicateMetric(rc, requestBody.getString("metric"))
            .onComplete(existsHandler -> {
                if (!rc.failed()) {
                    submitCreate(rc, requestBody);
                }
            });
    }

    public void get(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(
                id ->  metricService.findOne(id)
                    .onComplete(
                        handler -> ResultHandler.handleGetOneRequest(rc, handler)),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    public void all(RoutingContext rc) {
        metricService.findAll()
            .onComplete(handler -> ResultHandler.handleGetAllRequest(rc, handler));
    }

    public void patch(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(id -> checkUpdateExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    public void delete(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(
                id -> checkDeleteResourceExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    private void submitCreate(RoutingContext rc, JsonObject requestBody) {
        metricService.save(requestBody)
            .onComplete(handler -> ResultHandler.handleSaveRequest(rc, handler));
    }

    private void submitUpdate(RoutingContext rc, JsonObject requestBody,
        JsonObject entity) {
        for (String field : requestBody.fieldNames()) {
            entity.put(field, requestBody.getValue(field));
        }
        metricService.update(entity)
            .onComplete(updateHandler -> ResultHandler.handleUpdateDeleteRequest(rc, updateHandler));
    }

    private void submitDelete(RoutingContext rc, long id) {
        metricService.delete(id)
            .onComplete(deleteHandler -> ResultHandler.handleUpdateDeleteRequest(rc, deleteHandler));
    }

    private Future<Boolean> checkForDuplicateMetric(RoutingContext rc, String metric) {
        return metricService.existsOneByMetric(metric)
            .onComplete(duplicateHandler -> ErrorHandler.handleDuplicates(rc, duplicateHandler));
    }

    private void checkUpdateExists(RoutingContext rc, long id) {
        metricService.findOne(id)
            .onComplete(updateHandler -> ErrorHandler.handleFindOne(rc, updateHandler))
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

    private void checkDeleteResourceExists(RoutingContext rc, long id) {
        metricService.existsOneById(id)
            .onComplete(findHandler -> ErrorHandler.handleExistsOne(rc, findHandler))
            .onComplete(findHandler -> {
                if (!rc.failed()) {
                    submitDelete(rc, id);
                }
            });
    }
}
