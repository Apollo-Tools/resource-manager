package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.metric.MetricService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class MetricChecker extends EntityChecker {

    private final MetricService metricService;

    public MetricChecker(MetricService metricService) {
        super(metricService);
        this.metricService = metricService;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByMetric = metricService.existsOneByMetric(entity.getString("metric"));
        return ErrorHandler.handleDuplicates(existsOneByMetric).ignoreElement();
    }

    @Override
    public Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("metric")) {
            return this.checkForDuplicateEntity(requestBody)
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }
}
