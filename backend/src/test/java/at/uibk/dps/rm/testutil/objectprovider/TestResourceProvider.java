package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;

/**
 * Utility class to instantiate objects that are linked to the resource entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestResourceProvider {
    public static Resource createResource(long id, ResourceType resourceType, Region region) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        resource.setRegion(region);
        return resource;
    }

    public static Resource createResource(long id, ResourceType resourceType) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L);
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        return createResource(id, resourceType, region);
    }

    public static Resource createResource(long resourceId) {
        ResourceType resourceType = createResourceType(1L, "cloud");
        return createResource(resourceId, resourceType);
    }

    public static Resource createResource(long resourceId, Region region) {
        ResourceType resourceType = createResourceType(1L, "cloud");
        return createResource(resourceId, resourceType, region);
    }

    public static Resource createResourceLambda(long id, Region region, Double timeout, Double memorySize) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        resource.setRegion(region);
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.LAMBDA.getValue());
        resource.setPlatform(platform);
        MetricType mt1 = TestMetricProvider.createMetricTypeNumber();
        MetricType mt2 = TestMetricProvider.createMetricTypeString();
        Metric m1 = TestMetricProvider.createMetric(1L, "timeout", mt1, false);
        Metric m2 = TestMetricProvider.createMetric(2L, "memory-size", mt1, false);
        Metric m3 = TestMetricProvider.createMetric(3L, "deployment-role", mt2, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, timeout);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, m2, memorySize);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, m3, "labRole");
        resource.setMetricValues(Set.of(mv1, mv2, mv3));
        return resource;
    }

    public static Resource createResourceLambda(long id, Double timeout, Double memorySize) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        return createResourceLambda(id, region, timeout, memorySize);
    }

    public static Resource createResourceEC2(long id, Region region, Double timeout, Double memorySize,
            String instanceType) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        resource.setRegion(region);
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.EC2.getValue());
        resource.setPlatform(platform);
        MetricType mt1 = TestMetricProvider.createMetricTypeNumber();
        MetricType mt2 = TestMetricProvider.createMetricTypeString();
        Metric m1 = TestMetricProvider.createMetric(1L, "timeout", mt1, false);
        Metric m2 = TestMetricProvider.createMetric(2L, "memory-size", mt1, false);
        Metric m3 = TestMetricProvider.createMetric(3L, "instance-type", mt2, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, timeout);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, m2, memorySize);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, m3, instanceType);
        resource.setMetricValues(Set.of(mv1, mv2, mv3));
        return resource;
    }

    public static Resource createResourceEC2(long id, Double timeout, Double memorySize,
            String instanceType) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        return createResourceEC2(id, region, timeout, memorySize, instanceType);
    }

    public static Resource createResourceOpenFaas(long id, Region region, Double timeout, Double memorySize,
            String gatewayUrl, String openfaasUser, String openfaasPw) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        resource.setRegion(region);
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.OPENFAAS.getValue());
        resource.setPlatform(platform);
        MetricType mt1 = TestMetricProvider.createMetricTypeNumber();
        MetricType mt2 = TestMetricProvider.createMetricTypeString();
        Metric m1 = TestMetricProvider.createMetric(1L, "timeout", mt1, false);
        Metric m2 = TestMetricProvider.createMetric(2L, "memory-size", mt1, false);
        Metric m3 = TestMetricProvider.createMetric(3L, "gateway-url", mt2, false);
        Metric m4 = TestMetricProvider.createMetric(4L, "openfaas-user", mt2, false);
        Metric m5 = TestMetricProvider.createMetric(5L, "openfaas-pw", mt2, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, timeout);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, m2, memorySize);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, m3, gatewayUrl);
        MetricValue mv4 = TestMetricProvider.createMetricValue(4L, m4, openfaasUser);
        MetricValue mv5 = TestMetricProvider.createMetricValue(5L, m5, openfaasPw);
        resource.setMetricValues(Set.of(mv1, mv2, mv3, mv4, mv5));
        return resource;
    }

    public static Resource createResourceOpenFaas(long id, Double timeout, Double memorySize,
        String gatewayUrl, String openfaasUser, String openfaasPw) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        return createResourceOpenFaas(id, region, timeout, memorySize, gatewayUrl, openfaasUser, openfaasPw);
    }


    public static Resource createResourceFaaS(long id, Region region, Double timeout, Double memorySize) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        resource.setRegion(region);
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
        resource.setRegion(region);
        MetricType mt = TestMetricProvider.createMetricType(1L, "string");
        Metric m1 = TestMetricProvider.createMetric(1L, "instance-type", mt, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, instanceType);
        resource.setMetricValues(Set.of(mv1));
        return resource;
    }

    public static Resource createResourceEdge(long id, String gatewayUrl, String openFaasUser, String openfaasPw) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        ResourceProvider provider = TestResourceProviderProvider.createResourceProvider(1L, "edge");
        Region region = TestResourceProviderProvider.createRegion(2L, "edge", provider);
        resource.setRegion(region);
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

    public static Resource createResourceContainer(long id, Region region, String clusterUrl, boolean hasExternalIp) {
        Resource resource = new Resource();
        resource.setResourceId(id);
        resource.setRegion(region);
        Platform platform = TestPlatformProvider.createPlatformContainer(1L, PlatformEnum.K8S.getValue());
        resource.setPlatform(platform);
        MetricType mt1 = TestMetricProvider.createMetricTypeNumber();
        MetricType mt2 = TestMetricProvider.createMetricTypeString();
        Metric m1 = TestMetricProvider.createMetric(1L, "cluster-url", mt2, false);
        Metric m2 = TestMetricProvider.createMetric(2L, "pre-pull-timeout", mt1, false);
        Metric m3 = TestMetricProvider.createMetric(3L, "external-ip", mt2, false);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, clusterUrl);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, m2, 2);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, m3, "0.0.0.0");
        if (hasExternalIp) {
            resource.setMetricValues(Set.of(mv1, mv2, mv3));
        } else {
            resource.setMetricValues(Set.of(mv1, mv2));
        }

        return resource;
    }

    public static Resource createResourceContainer(long id, String clusterUrl, boolean hasExternalIp) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        return createResourceContainer(id, region, clusterUrl, hasExternalIp);
    }

    public static ResourceType createResourceType(long resourceTypeId, String resourceTypeLabel) {
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(resourceTypeId);
        resourceType.setResourceType(resourceTypeLabel);
        return resourceType;
    }

    public static ResourceType createResourceTypeFaas(long resourceTypeId) {
        return createResourceType(resourceTypeId, ResourceTypeEnum.FAAS.getValue());
    }

    public static ResourceType createResourceTypeContainer(long resourceTypeId) {
        return createResourceType(resourceTypeId, ResourceTypeEnum.CONTAINER.getValue());
    }

    public static JsonArray createGetAllResourcesArray() {
        Resource resource1 = TestResourceProvider.createResource(1L);
        Resource resource2 = TestResourceProvider.createResource(2L);
        Resource resource3 = TestResourceProvider.createResource(3L);
        return new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
                JsonObject.mapFrom(resource3)));
    }
}
