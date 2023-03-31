package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.*;

import java.util.Set;

public class TestResourceProvider {
    public static Resource createResource(long id, ResourceType resourceType, Region region, boolean selfManaged) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        resource.setResourceType(resourceType);
        resource.setRegion(region);
        resource.setIsSelfManaged(selfManaged);
        return resource;
    }

    public static Resource createResource(long id, ResourceType resourceType) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L);
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        return createResource(id, resourceType, region, false);
    }

    public static Resource createResource(long resourceId) {
        ResourceType resourceType = createResourceType(1L, "cloud");
        return createResource(resourceId, resourceType);
    }

    public static Resource createResourceFaaS(long id, Region region, Double timeout, Double memorySize) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        ResourceType rt = createResourceType(1L, "faas");
        resource.setResourceType(rt);
        resource.setRegion(region);
        resource.setIsSelfManaged(false);
        MetricType mt = TestMetricProvider.createMetricType(1L, "number");
        Metric m1 = TestMetricProvider.createMetric(1L, "timeout", mt, false);
        Metric m2 = TestMetricProvider.createMetric(2L, "memory-size", mt, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, timeout);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, m2, memorySize);
        resource.setMetricValues(Set.of(mv1, mv2));
        return resource;
    }

    public static Resource createResourceVM(long id, Region region, String instanceType) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        ResourceType rt = createResourceType(2L, "vm");
        resource.setResourceType(rt);
        resource.setRegion(region);
        resource.setIsSelfManaged(false);
        MetricType mt = TestMetricProvider.createMetricType(1L, "string");
        Metric m1 = TestMetricProvider.createMetric(1L, "instance-type", mt, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, instanceType);
        resource.setMetricValues(Set.of(mv1));
        return resource;
    }

    public static Resource createResourceEdge(long id, String gatewayUrl, String openFaasUser, String openfaasPw) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        ResourceType rt = createResourceType(2L, "edge");
        resource.setResourceType(rt);
        ResourceProvider provider = TestResourceProviderProvider.createResourceProvider(1L, "edge");
        Region region = TestResourceProviderProvider.createRegion(2L, "edge", provider);
        resource.setRegion(region);
        resource.setIsSelfManaged(false);
        MetricType mt = TestMetricProvider.createMetricType(1L, "string");
        Metric m1 = TestMetricProvider.createMetric(1L, "gateway-url", mt, false);
        Metric m2 = TestMetricProvider.createMetric(2L, "openfaas-user", mt, false);
        Metric m3 = TestMetricProvider.createMetric(3L, "openfaas-pw", mt, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, gatewayUrl);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, m2, openFaasUser);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, m3, openfaasPw);
        resource.setMetricValues(Set.of(mv1, mv2, mv3));
        return resource;
    }

    public static ResourceType createResourceType(long resourceTypeId, String resourceTypeLabel) {
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(resourceTypeId);
        resourceType.setResourceType(resourceTypeLabel);
        return resourceType;
    }
}
