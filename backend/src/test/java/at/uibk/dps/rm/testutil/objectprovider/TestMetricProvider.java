package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.PlatformMetric;
import lombok.experimental.UtilityClass;

/**
 * Utility class to instantiate objects that are linked to the metric entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestMetricProvider {
    public static MetricType createMetricType(long metricTypeId, String metricTypeName) {
        MetricType metricType = new MetricType();
        metricType.setMetricTypeId(metricTypeId);
        metricType.setType(metricTypeName);
        return metricType;
    }

    public static MetricType createMetricTypeNumber() {
        return createMetricType(1L, "number");
    }

    public static MetricType createMetricTypeString() {
        return createMetricType(2L, "string");
    }

    public static MetricType createMetricTypeBoolean() {
        return createMetricType(3L, "boolean");
    }

    public static Metric createMetric(long metricId, String metricName, long metricTypeId, String metricType,
                                      boolean isMonitored) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(createMetricType(metricTypeId, metricType));
        metric.setDescription("Blah");
        metric.setIsMonitored(isMonitored);
        return metric;
    }

    public static Metric createMetric(long metricId, String metricName, MetricType metricType, boolean isMonitored) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(metricType);
        metric.setDescription("Blah");
        metric.setIsMonitored(isMonitored);
        return metric;
    }

    public static Metric createMetric(long metricId, String metricName) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(createMetricTypeNumber());
        metric.setDescription("Blah");
        metric.setIsMonitored(false);
        return metric;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, double value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric);
        metricValue.setValueNumber(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, Metric metric, double value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metric);
        metricValue.setValueNumber(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, String value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric);
        metricValue.setValueString(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, Metric metric, String value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metric);
        metricValue.setValueString(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, Metric metric, boolean value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metric);
        metricValue.setValueBool(value);
        return metricValue;
    }

    private static void initMetricValue(MetricValue metricValue, long metricValueId, long metricId, String metric) {
        initMetricValue(metricValue, metricValueId, createMetric(metricId, metric));
    }

    private static void initMetricValue(MetricValue metricValue, long metricValueId, Metric metric) {
        metricValue.setMetricValueId(metricValueId);
        metricValue.setMetric(metric);
        metricValue.setCount(10L);
    }

    public static PlatformMetric createPlatformMetric(long platformMetricId, long metricId) {
        PlatformMetric platformMetric = new PlatformMetric();
        platformMetric.setPlatformMetricId(platformMetricId);
        platformMetric.setPlatform(TestPlatformProvider.createPlatformFaas(1L, "platform"));
        platformMetric.setMetric(createMetric(metricId, "metric" + metricId));
        platformMetric.setIsMainResourceMetric(true);
        platformMetric.setIsSubResourceMetric(true);
        platformMetric.setRequired(true);
        return platformMetric;
    }
}
