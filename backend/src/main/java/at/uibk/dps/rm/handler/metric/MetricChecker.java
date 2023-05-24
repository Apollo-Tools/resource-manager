package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the metric entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class MetricChecker extends EntityChecker {

    private final MetricService metricService;

    /**
     * Create an instance from the metricService.
     *
     * @param metricService the metric service
     */
    public MetricChecker(MetricService metricService) {
        super(metricService);
        this.metricService = metricService;
    }

    /**
     * Find all metrics by resource type and if they are required or optional.
     *
     * @param resourceTypeId the id of the resource type
     * @param required whether the metrics are required or optional
     * @return a Single that emits all found metrics as JsonArray
     */
    public Single<JsonArray> checkFindAllByResourceTypeId(long resourceTypeId, boolean required) {
        Single<JsonArray> findAllByResourceTypeId = metricService.findAllByResourceTypeId(resourceTypeId,
            required);
        return ErrorHandler.handleFindAll(findAllByResourceTypeId);
    }

    /**
     * Find a metric by its name.
     *
     * @param metric the name of the metric
     * @return a Single that emits the found metric as JsonObject
     */
    public Single<JsonObject> checkFindOneByMetric(String metric) {
        Single<JsonObject> findOneByMetric = metricService.findOneByMetric(metric);
        return ErrorHandler.handleFindOne(findOneByMetric);
    }

    /**
     * Check whether the value type of a service level objective and metric match.
     *
     * @param slo the service level objective
     * @param metric the metric
     * @return a Single that emits true if the value type match, else false
     */
    public Single<Boolean> checkEqualValueTypes(ServiceLevelObjective slo, JsonObject metric) {
        String sloValueType = slo.getValue().get(0).getSloValueType().name();
        String metricValueType = metric.getJsonObject("metric_type")
            .getString("type").toUpperCase();
        boolean checkForTypeMatch = sloValueType.equals(metricValueType);
        return ErrorHandler.handleBadInput(Single.just(checkForTypeMatch));
    }

    /***
     * Check whether the value of a metric to add is set with the correct value type
     *
     * @param requestEntry the value from the request
     * @param metricId the id of the metric
     * @param metricValue the metric value to set
     * @return a Completable
     */
    public Completable checkAddMetricValueSetCorrectly(JsonObject requestEntry, long metricId,
        MetricValue metricValue) {
        Single<Boolean> checkMetricValue = checkFindOne(metricId)
            .map(result -> {
                Metric metric = result.mapTo(Metric.class);
                Object value = requestEntry.getValue("value");
                boolean valueHasCorrectType = true;
                if (metric.getIsMonitored()) {
                    setDefaultMonitoredMetricValue(metricValue, metric);
                }
                else if (stringMetricHasStringValue(metric, value)) {
                    metricValue.setValueString((String) value);
                } else if (numberMetricHasNumberValue(metric, value)) {
                    metricValue.setValueNumber(requestEntry.getNumber("value").doubleValue());
                } else if (boolMetricHasNumberValue(metric, value)) {
                    metricValue.setValueBool((Boolean) value);
                } else {
                    valueHasCorrectType = false;
                }
                return valueHasCorrectType;
            });
        return ErrorHandler.handleBadInput(checkMetricValue).ignoreElement();
    }

    /***
     * Check whether the updated value of an already linked metric is set with the correct value type
     *
     * @param requestEntry the value from the request
     * @param metricId the id of the metric
     * @return a Completable
     */
    public Completable checkUpdateMetricValueSetCorrectly(JsonObject requestEntry, long metricId) {
        Single<Boolean> checkMetricValue = checkFindOne(metricId)
            .map(result -> {
                Metric metric = result.mapTo(Metric.class);
                Object value = requestEntry.getValue("value");
                return !metric.getIsMonitored() &&
                    (stringMetricHasStringValue(metric, value) ||
                        numberMetricHasNumberValue(metric, value) ||
                        boolMetricHasNumberValue(metric, value));
            });
        return ErrorHandler.handleBadInput(checkMetricValue).ignoreElement();
    }

    /**
     * Check if a value is a string and if the metric type is string as well.
     *
     * @param metric the metric
     * @param value the value
     * @return true if the booth are strings, else false
     */
    private boolean stringMetricHasStringValue(Metric metric, Object value) {
        return value instanceof String && metric.getMetricType().getType().equals("string");
    }

    /**
     * Check if a value is a number and if the metric type is number as well.
     *
     * @param metric the metric
     * @param value the value
     * @return true if the booth are numbers, else false
     */
    private boolean numberMetricHasNumberValue(Metric metric, Object value) {
        return value instanceof Number && metric.getMetricType().getType().equals("number");
    }

    /**
     * Check if a value is a boolean and if the metric type is boolean as well.
     *
     * @param metric the metric
     * @param value the value
     * @return true if the booth are booleans, else false
     */
    private boolean boolMetricHasNumberValue(Metric metric, Object value) {
        return value instanceof Boolean && metric.getMetricType().getType().equals("boolean");
    }

    /**
     * Set a default metric value depending on its type.
     *
     * @param metricValue the metric value
     * @param metric the metric
     */
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

    /**
     * Check if the metric of a service level objective exists and if the value types are equal.
     *
     * @param slo the service level objective
     * @return a Completable
     */
    public Completable checkServiceLevelObjectives(ServiceLevelObjective slo) {
        return checkFindOneByMetric(slo.getName())
            .flatMap(metric -> checkEqualValueTypes(slo, metric))
            .ignoreElement();
    }
}
