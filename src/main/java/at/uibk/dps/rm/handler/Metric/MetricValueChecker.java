package at.uibk.dps.rm.handler.Metric;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.metric.MetricValueService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class MetricValueChecker extends EntityChecker {

    private final MetricValueService metricValueService;


    public MetricValueChecker(MetricValueService metricValueService) {
        super(metricValueService);
        this.metricValueService = metricValueService;
    }


    public Single<JsonArray> checkFindAll(long id) {
        return metricValueService.findAllByResource(id);
    }

    public Completable submitDeleteMetricValue(long resourceId, long metricId) {
        return metricValueService.deleteByResourceAndMetric(resourceId, metricId);
    }

    public Completable checkForDuplicateByResourceAndMetric(long resourceId, long metricId) {
        Single<Boolean> existsOneByResourceAndMetric = metricValueService.existsOneByResourceAndMetric(resourceId, metricId);
        return ErrorHandler.handleDuplicates(existsOneByResourceAndMetric).ignoreElement();
    }

    public Completable checkMetricValueExistsByResourceAndMetric(long resourceId, long metricId) {
        Single<Boolean> existsOneByResourceAndMetric =
            metricValueService.existsOneByResourceAndMetric(resourceId, metricId);
        return ErrorHandler.handleExistsOne(existsOneByResourceAndMetric).ignoreElement();
    }
}
