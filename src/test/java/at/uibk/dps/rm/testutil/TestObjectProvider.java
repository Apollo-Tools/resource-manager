package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.entity.dto.GetResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestObjectProvider {

    public static Resource createResource(long resourceId, ResourceType resourceType, boolean selfManaged) {
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        resource.setResourceType(resourceType);
        resource.setSelfManaged(true);
        return  resource;
    }

    public static Resource createResource(long resourceId) {
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        resource.setResourceType(createResourceType(1L, "cloud"));
        resource.setSelfManaged(false);
        resource.setMetricValues(new HashSet<>());
        return  resource;
    }

    public static ResourceType createResourceType(long resourceTypeId, String resourceTypeLabel) {
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(resourceTypeId);
        resourceType.setResource_type(resourceTypeLabel);
        return resourceType;
    }

    public static MetricType createMetricType(long metricTypeId, String metricTypeName) {
        MetricType metricType = new MetricType();
        metricType.setMetricTypeId(metricTypeId);
        metricType.setType(metricTypeName);
        return metricType;
    }

    public static Metric createMetric(long metricId, String metricName, MetricType metricType,boolean isMonitored) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(metricType);
        metric.setDescription("Blah");
        metric.setMonitored(isMonitored);
        return metric;
    }

    public static Metric createMetric(long metricId, String metricName) {
        Metric metric = new Metric();
        metric.setMetricId(metricId);
        metric.setMetric(metricName);
        metric.setMetricType(createMetricType(1L, "number"));
        metric.setDescription("Blah");
        metric.setMonitored(false);
        return metric;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, double value, Resource resource) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric, resource);
        metricValue.setValueNumber(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, String value, Resource resource) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric, resource);
        metricValue.setValueString(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, boolean value, Resource resource) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric, resource);
        metricValue.setValueBool(value);
        return metricValue;
    }

    private static void initMetricValue(MetricValue metricValue, long metricValueId, long metricId, String metric, Resource resource) {
        metricValue.setMetricValueId(metricValueId);
        metricValue.setMetric(createMetric(metricId, metric));
        metricValue.setResource(resource);
        metricValue.setCount(10L);
    }

    public static SLOValue createSLOValue(double value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.NUMBER);
        sloValue.setValueNumber(value);
        return sloValue;
    }

    public static SLOValue createSLOValue(String value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.STRING);
        sloValue.setValuerString(value);
        return sloValue;
    }

    public static SLOValue createSLOValue(boolean value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.BOOLEAN);
        sloValue.setValueBool(value);
        return sloValue;
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType, double... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (double v: value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType, String... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (String v: value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType, boolean... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (boolean v: value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static GetResourcesBySLOsRequest createResourceBySLOsRequest(List<ServiceLevelObjective> serviceLevelObjectives, int limit) {
        GetResourcesBySLOsRequest request = new GetResourcesBySLOsRequest();
        request.setServiceLevelObjectives(serviceLevelObjectives);
        request.setLimit(limit);
        return request;
    }
}
