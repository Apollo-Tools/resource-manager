package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.metric.MetricTypeEnum;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * A utility class that provides different methods to validate metric values.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class MetricValueUtility {

    private final MetricValueRepository repository;

    private final PlatformMetricRepository platformMetricRepository;


    /**
     * Check new metric values for violations and create them if none occur.
     *
     * @param resource the resource
     * @param values the values to add to the resource
     * @return a List of completable futures that emit nothing
     */
    public Single<List<Completable>> checkAddMetricList(SessionManager sessionManager, Resource resource,
            JsonArray values) {
        return Observable.fromIterable(values)
            .map(jsonObject -> {
                JsonObject jsonMetric = (JsonObject) jsonObject;
                long metricId = jsonMetric.getLong("metric_id");
                MetricValue metricValue = new MetricValue();
                return repository.findByResourceAndMetric(sessionManager, resource.getResourceId(), metricId)
                    .flatMap(existingValue -> Maybe.<PlatformMetric>error(new AlreadyExistsException(MetricValue.class)))
                    .switchIfEmpty(platformMetricRepository.findByPlatformAndMetric(sessionManager,
                        resource.getMain().getPlatform().getPlatformId(), metricId))
                    .switchIfEmpty(Maybe.error(new NotFoundException(PlatformMetric.class)))
                    .flatMapCompletable(platformMetric -> {
                        metricValue.setMetric(platformMetric.getMetric());
                        metricValue.setResource(resource);
                        checkAddMetricValueSetCorrectly(platformMetric, jsonMetric, metricValue);
                        return sessionManager.persist(metricValue).ignoreElement();
                    });
            })
            .toList();
    }


    /***
     * Check whether the value of a metric value to add is set with the correct value type
     *
     * @param platformMetric the platform metric
     * @param jsonValue the value
     * @param metricValue the metric value to set
     */
    public void checkAddMetricValueSetCorrectly(PlatformMetric platformMetric, JsonObject jsonValue,
            MetricValue metricValue) {
        Object value = jsonValue.getValue("value");
        boolean valueHasCorrectType = true;
        Metric metric = platformMetric.getMetric();
        if (platformMetric.getIsMonitored() && value != null) {
            throw new BadInputException("monitored metrics can't be set manually");
        } else if (platformMetric.getIsMonitored()) {
            setDefaultMonitoredMetricValue(metricValue, metric);
        }
        else if (stringMetricHasStringValue(metric, value)) {
            metricValue.setValueString((String) value);
        } else if (numberMetricHasNumberValue(metric, value)) {
            metricValue.setValueNumber(jsonValue.getNumber("value").doubleValue());
        } else if (boolMetricHasNumberValue(metric, value)) {
            metricValue.setValueBool((Boolean) value);
        } else {
            valueHasCorrectType = false;
        }
        if (!valueHasCorrectType) {
            throw new BadInputException("invalid value type for metric (" + metric.getMetricId() + ")");
        }
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
     * Check if a value is a string and if the metric type is string as well.
     *
     * @param metricType the metric type
     * @param value the value
     * @return true if the types match, else false
     */
    public boolean metricTypeMatchesValue(MetricTypeEnum metricType, String value) {
        return value!=null && metricType.equals(MetricTypeEnum.STRING);
    }

    /**
     * @see #metricTypeMatchesValue(MetricTypeEnum metricType, String value)
     */
    public boolean metricTypeMatchesValue(MetricTypeEnum metricType, Double value) {
        return value!=null && metricType.equals(MetricTypeEnum.NUMBER);
    }

    /**
     * @see #metricTypeMatchesValue(MetricTypeEnum metricType, String value)
     */
    public boolean metricTypeMatchesValue(MetricTypeEnum metricType, Boolean value) {
        return value!=null && metricType.equals(MetricTypeEnum.BOOLEAN);
    }
}
