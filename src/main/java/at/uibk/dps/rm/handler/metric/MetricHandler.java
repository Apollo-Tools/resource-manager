package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricTypeService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class MetricHandler extends ValidationHandler {

    private final MetricTypeChecker metricTypeChecker;

    public MetricHandler(MetricService metricService, MetricTypeService metricTypeService) {
        super(new MetricChecker(metricService));
        metricTypeChecker = new MetricTypeChecker(metricTypeService);
    }

    @Override
    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return entityChecker.checkForDuplicateEntity(requestBody)
                .andThen(metricTypeChecker.checkExistsOne(requestBody.getJsonObject("metric_type").getLong("metric_type_id")))
                .andThen(entityChecker.submitCreate(requestBody));
    }
}
