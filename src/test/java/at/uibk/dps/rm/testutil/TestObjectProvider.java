package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.entity.dto.GetResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class TestObjectProvider {

    public static Account createAccount(long accountId, String username, String password) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername(username);
        account.setPassword(password);
        account.setIsActive(true);
        return account;
    }

    public static Resource createResource(long resourceId, ResourceType resourceType, boolean selfManaged) {
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        resource.setResourceType(resourceType);
        resource.setIsSelfManaged(selfManaged);
        return resource;
    }

    public static Resource createResource(long resourceId) {
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        resource.setResourceType(createResourceType(1L, "cloud"));
        resource.setIsSelfManaged(false);
        resource.setMetricValues(new ArrayList<>());
        return resource;
    }

    public static ResourceType createResourceType(long resourceTypeId, String resourceTypeLabel) {
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(resourceTypeId);
        resourceType.setResourceType(resourceTypeLabel);
        return resourceType;
    }

    public static MetricType createMetricType(long metricTypeId, String metricTypeName) {
        MetricType metricType = new MetricType();
        metricType.setMetricTypeId(metricTypeId);
        metricType.setType(metricTypeName);
        return metricType;
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
        metric.setMetricType(createMetricType(1L, "number"));
        metric.setDescription("Blah");
        metric.setIsMonitored(false);
        return metric;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, double value,
                                                Resource resource) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric, resource);
        metricValue.setValueNumber(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, String value,
                                                Resource resource) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric, resource);
        metricValue.setValueString(value);
        return metricValue;
    }

    public static MetricValue createMetricValue(long metricValueId, long metricId, String metric, boolean value,
                                                Resource resource) {
        MetricValue metricValue = new MetricValue();
        initMetricValue(metricValue, metricValueId, metricId, metric, resource);
        metricValue.setValueBool(value);
        return metricValue;
    }

    private static void initMetricValue(MetricValue metricValue, long metricValueId, long metricId, String metric,
                                        Resource resource) {
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
        sloValue.setValueString(value);
        return sloValue;
    }

    public static SLOValue createSLOValue(boolean value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.BOOLEAN);
        sloValue.setValueBool(value);
        return sloValue;
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    double... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (double v : value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    String... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (String v : value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    boolean... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (boolean v : value) {
            SLOValue sloValue = TestObjectProvider.createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static GetResourcesBySLOsRequest createResourceBySLOsRequest(List<ServiceLevelObjective> slos,
                                                                        int limit) {
        GetResourcesBySLOsRequest request = new GetResourcesBySLOsRequest();
        request.setServiceLevelObjectives(slos);
        request.setLimit(limit);
        return request;
    }

    public static ResourceReservation createResourceReservation(long id, Resource resource, Reservation reservation,
                                                                boolean isDeployed) {
        ResourceReservation resourceReservation = new ResourceReservation();
        resourceReservation.setResourceReservationId(id);
        resourceReservation.setResource(resource);
        resourceReservation.setReservation(reservation);
        resourceReservation.setIsDeployed(isDeployed);
        return resourceReservation;
    }

    public static Reservation createReservation(long id, boolean isActive, Account account) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(id);
        reservation.setIsActive(isActive);
        reservation.setCreatedBy(account);
        return  reservation;
    }

    public static ReserveResourcesRequest createReserveResourcesRequest(List<Long> resources, boolean deployResources) {
        ReserveResourcesRequest request = new ReserveResourcesRequest();
        request.setResources(resources);
        request.setDeployResources(deployResources);
        return request;
    }

    public static List<JsonObject> createResourceReservationsJson(Reservation reservation) {
        Resource resource1 = TestObjectProvider.createResource(1L);
        Resource resource2 = TestObjectProvider.createResource(2L);
        Resource resource3 = TestObjectProvider.createResource(3L);

        ResourceReservation resourceReservation1 = TestObjectProvider.createResourceReservation(1L, resource1,
            reservation, false);
        ResourceReservation resourceReservation2 = TestObjectProvider.createResourceReservation(2L, resource2,
            reservation, true);
        ResourceReservation resourceReservation3 = TestObjectProvider.createResourceReservation(3L, resource3,
            reservation, true);
        return List.of(JsonObject.mapFrom(resourceReservation1), JsonObject.mapFrom(resourceReservation2),
            JsonObject.mapFrom(resourceReservation3));
    }
}
