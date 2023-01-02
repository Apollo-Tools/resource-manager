package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
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

    public Single<JsonObject> checkFindOneByMetric(String metric) {
        Single<JsonObject> findOneByMetric = metricService.findOneByMetric(metric);
        return ErrorHandler.handleFindOne(findOneByMetric);
    }

    public Single<Boolean> checkEqualValueTypes(ServiceLevelObjective slo, JsonObject metric) {
        String sloValueType = slo.getValue().get(0).getSloValueType().name();
        String metricValueType = metric.getJsonObject("metric_type")
            .getString("type").toUpperCase();
        boolean checkForTypeMatch = sloValueType.equals(metricValueType);
        return ErrorHandler.handleBadInput(Single.just(checkForTypeMatch));
    }

    public Completable checkAddMetricValueSetCorrectly(JsonObject jsonEntry, long metricId, MetricValue metricValue) {
        Single<Boolean> checkMetricValue = checkFindOne(metricId)
            .map(result -> {
                Metric metric = result.mapTo(Metric.class);
                Object value = jsonEntry.getValue("value");
                boolean valueHasCorrectType = true;
                if (metric.getIsMonitored()) {
                    setDefaultMonitoredMetricValue(metricValue, metric);
                }
                else if (stringMetricHasStringValue(metric, value)) {
                    metricValue.setValueString((String) value);
                } else if (numberMetricHasNumberValue(metric, value)) {
                    metricValue.setValueNumber(jsonEntry.getNumber("value").doubleValue());
                } else if (boolMetricHasNumberValue(metric, value)) {
                    metricValue.setValueBool((Boolean) value);
                } else {
                    valueHasCorrectType = false;
                }
                return valueHasCorrectType;
            });
        return ErrorHandler.handleBadInput(checkMetricValue).ignoreElement();
    }

    public Completable checkUpdateMetricValueSetCorrectly(JsonObject jsonEntry, long metricId) {
        Single<Boolean> checkMetricValue = checkFindOne(metricId)
            .map(result -> {
                Metric metric = result.mapTo(Metric.class);
                Object value = jsonEntry.getValue("value");
                return !metric.getIsMonitored() &&
                    (stringMetricHasStringValue(metric, value) ||
                        numberMetricHasNumberValue(metric, value) ||
                        boolMetricHasNumberValue(metric, value));
            });
        return ErrorHandler.handleBadInput(checkMetricValue).ignoreElement();
    }

    private boolean stringMetricHasStringValue(Metric metric, Object value) {
        return value instanceof String && metric.getMetricType().getType().equals("string");
    }

    private boolean numberMetricHasNumberValue(Metric metric, Object value) {
        return value instanceof Number && metric.getMetricType().getType().equals("number");
    }

    private boolean boolMetricHasNumberValue(Metric metric, Object value) {
        return value instanceof Boolean && metric.getMetricType().getType().equals("boolean");
    }

    private void setDefaultMonitoredMetricValue (MetricValue metricValue, Metric metric) {
        switch (metric.getMetricType().getType()) {
            case "number":
                metricValue.setValueNumber(0.0);
                break;
            case "string":
                metricValue.setValueString("");
                break;
            case "boolean":
                metricValue.setValueBool(false);
        }
    }
}
