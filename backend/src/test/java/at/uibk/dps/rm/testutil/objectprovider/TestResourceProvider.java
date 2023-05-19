package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
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

    public static Resource createResource(long resourceId, Region region) {
        ResourceType resourceType = createResourceType(1L, "cloud");
        return createResource(resourceId, resourceType, region, false);
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

    public static Resource createResourceContainer(long id, String clusterUrl, long replicas, double cpu,
            long memorySize) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        ResourceType rt = createResourceType(3L, "container");
        resource.setResourceType(rt);
        ResourceProvider provider = TestResourceProviderProvider.createResourceProvider(2L, "container");
        Region region = TestResourceProviderProvider.createRegion(3L, "container", provider);
        resource.setRegion(region);
        resource.setIsSelfManaged(false);
        MetricType mt1 = TestMetricProvider.createMetricType(1L, "string");
        MetricType mt2 = TestMetricProvider.createMetricType(1L, "number");
        Metric m1 = TestMetricProvider.createMetric(1L, "cluster-url", mt1, false);
        Metric m2 = TestMetricProvider.createMetric(2L, "replicas", mt2, false);
        Metric m3 = TestMetricProvider.createMetric(3L, "cpu", mt2, false);
        Metric m4 = TestMetricProvider.createMetric(4L, "memory-size", mt2, false);
        Metric m5 = TestMetricProvider.createMetric(4L, "pre-pull-timeout", mt2, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, clusterUrl);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, m2, replicas);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, m3, cpu);
        MetricValue mv4 = TestMetricProvider.createMetricValue(4L, m4, memorySize);
        MetricValue mv5 = TestMetricProvider.createMetricValue(5L, m5, 2);
        resource.setMetricValues(Set.of(mv1, mv2, mv3, mv4, mv5));

        return resource;
    }

    public static ResourceType createResourceType(long resourceTypeId, String resourceTypeLabel) {
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(resourceTypeId);
        resourceType.setResourceType(resourceTypeLabel);
        return resourceType;
    }

    public static JsonArray createGetAllResourcesArray() {
        Resource resource1 = TestResourceProvider.createResource(1L);
        Resource resource2 = TestResourceProvider.createResource(2L);
        Resource resource3 = TestResourceProvider.createResource(3L);
        return new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
                JsonObject.mapFrom(resource3)));
    }
}
