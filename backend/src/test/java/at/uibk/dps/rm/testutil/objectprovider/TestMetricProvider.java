package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
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

    public static Metric createMetric(Long metricId, String metricName, MetricType metricType) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(metricType);
        metric.setDescription("Blah");
        return metric;
    }

    public static Metric createMetric(long metricId, String metricName) {
        return createMetric(metricId, metricName, createMetricTypeNumber());
    }

    public static Metric createMetric(Long metricId) {
        return createMetric(metricId, "metric", createMetricTypeNumber());
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, double value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric);
        metricValue.setValueNumber(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(Long metricValueId, Metric metric, Resource resource, double value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metric);
        metricValue.setResource(resource);
        metricValue.setValueNumber(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(Long metricValueId, Metric metric, double value) {
        return createMetricValue(metricValueId, metric, TestResourceProvider.createResource(1L), value);
    }

    public static MetricValue createMetricValue(Long metricValueId, Metric metric, Resource resource, String value) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metric);
        metricValue.setResource(resource);
        metricValue.setValueString(value);
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

    private static void initMetricValue(MetricValue metricValue, Long metricValueId, long metricId, String metric) {
        initMetricValue(metricValue, metricValueId, createMetric(metricId, metric));
    }

    private static void initMetricValue(MetricValue metricValue, Long metricValueId, Metric metric) {
        metricValue.setMetricValueId(metricValueId);
        metricValue.setMetric(metric);
        metricValue.setCount(10L);
    }

    public static PlatformMetric createPlatformMetric(long platformMetricId, Metric metric, Platform platform,
            boolean isMonitored) {
        PlatformMetric platformMetric = new PlatformMetric();
        platformMetric.setPlatformMetricId(platformMetricId);
        platformMetric.setPlatform(platform);
        platformMetric.setMetric(metric);
        platformMetric.setIsMainResourceMetric(true);
        platformMetric.setIsSubResourceMetric(true);
        platformMetric.setIsMonitored(isMonitored);
        return platformMetric;
    }

    public static PlatformMetric createPlatformMetric(long platformMetricId, Metric metric) {
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, "platform");
        return createPlatformMetric(platformMetricId, metric, platform, true);
    }

    public static PlatformMetric createPlatformMetric(long platformMetricId, long metricId) {
        Metric metric = createMetric(metricId, "metric" + metricId);
        return createPlatformMetric(platformMetricId, metric);
    }

    public static MonitoredMetricValue createMonitoredMetricValue(MonitoringMetricEnum metricEnum, double value) {
        MonitoredMetricValue metricValue = new MonitoredMetricValue(metricEnum);
        metricValue.setValueNumber(value);
        return metricValue;
    }

    public static MonitoredMetricValue createMonitoredMetricValue(MonitoringMetricEnum metricEnum, String value) {
        MonitoredMetricValue metricValue = new MonitoredMetricValue(metricEnum);
        metricValue.setValueString(value);
        return metricValue;
    }

    public static MonitoredMetricValue createMonitoredMetricValue(MonitoringMetricEnum metricEnum, boolean value) {
        MonitoredMetricValue metricValue = new MonitoredMetricValue(metricEnum);
        metricValue.setValueBool(value);
        return metricValue;
    }
}
