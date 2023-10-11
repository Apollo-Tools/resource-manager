package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceDTO;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to instantiate objects that are linked to the resource entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestResourceProvider {

    public static MainResource createClusterWithNodes(long resourceId, String name, String node1, String node2,
                                                      String node3) {
        Platform platform = TestPlatformProvider.createPlatformContainer(1L, PlatformEnum.K8S.getValue());
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        MainResource mainResource = createResource(resourceId, platform, region).getMain();
        SubResource n1 = createSubResource(2L, node1, mainResource);
        SubResource n2 = createSubResource(3L, node2, mainResource);
        SubResource n3 = createSubResource(4L, node3, mainResource);
        mainResource.setSubResources(List.of(n1, n2, n3));
        mainResource.setName(name);
        Set<MetricValue> metricValues = new HashSet<>();
        metricValues.add(TestMetricProvider.createMetricValue(1L, 2L,"cpu", 1.0));
        metricValues.add(TestMetricProvider.createMetricValue(2L, 3L,"memory-size", 100));
        metricValues.add(TestMetricProvider.createMetricValue(3L, 4L,"storage-size", 100));
        metricValues.add(TestMetricProvider.createMetricValue(4L, 5L,"availability", 99.5));
        mainResource.setMetricValues(metricValues);
        return mainResource;
    }

    public static MainResource createClusterWithoutNodes(long resourceId, String name) {
        Platform platform = TestPlatformProvider.createPlatformContainer(1L, PlatformEnum.K8S.getValue());
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        MainResource mainResource = createResource(resourceId, platform, region).getMain();
        mainResource.setName(name);
        Set<MetricValue> metricValues = new HashSet<>();
        metricValues.add(TestMetricProvider.createMetricValue(1L, 2L,"cpu", 1.0));
        metricValues.add(TestMetricProvider.createMetricValue(2L, 3L,"memory-size", 100));
        metricValues.add(TestMetricProvider.createMetricValue(3L, 4L,"storage-size", 100));
        metricValues.add(TestMetricProvider.createMetricValue(4L, 5L,"availability", 99.5));
        mainResource.setMetricValues(metricValues);
        return mainResource;
    }

    public static SubResource createSubResource(long id, String name, MainResource mainResource) {
        SubResource subResource = new SubResource();
        subResource.setResourceId(id);
        subResource.setName(name);
        subResource.setMainResource(mainResource);
        Set<MetricValue> metricValues = new HashSet<>();
        metricValues.add(TestMetricProvider.createMetricValue(1L, 2L,"cpu", 1.0));
        metricValues.add(TestMetricProvider.createMetricValue(2L, 3L,"memory-size", 100));
        metricValues.add(TestMetricProvider.createMetricValue(3L, 4L,"storage-size", 100));
        metricValues.add(TestMetricProvider.createMetricValue(4L, 5L,"availability", 99.5));
        subResource.setMetricValues(metricValues);
        return subResource;
    }


    public static Resource createResource(Long id, Platform platform, Region region) {
        MainResource resource = new MainResource();
        resource.setName("r" + id);
        resource.setResourceId(id);
        resource.setRegion(region);
        resource.setPlatform(platform);
        resource.setMetricValues(new HashSet<>());
        return resource;
    }

    public static Resource createResource(long id, Platform platform) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L);
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        return createResource(id, platform, region);
    }

    public static Resource createResource(long resourceId) {
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, "lambda");
        return createResource(resourceId, platform);
    }

    public static Resource createResource(long resourceId, MetricValue... metricValues) {
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, "lambda");
        Resource resource = createResource(resourceId, platform);
        for (MetricValue metricValue : metricValues) {
            resource.getMetricValues().add(metricValue);
        }
        return resource;
    }

    public static Resource createResource(long resourceId, Region region) {
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, "lambda");
        return createResource(resourceId, platform, region);
    }

    public static Resource createResourceLambda(long id, Region region) {
        MainResource resource = new MainResource();
        resource.setResourceId(id);
        resource.setName("mainresource" + id);
        resource.setRegion(region);
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.LAMBDA.getValue());
        resource.setPlatform(platform);
        MetricType mt1 = TestMetricProvider.createMetricTypeString();
        Metric m1 = TestMetricProvider.createMetric(1L, "deployment-role", mt1);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, "labRole");
        resource.setMetricValues(Set.of(mv1));
        return resource;
    }

    public static Resource createResourceLambda(long id) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        return createResourceLambda(id, region);
    }

    public static Resource createResourceEC2(long id, Region region, String instanceType) {
        MainResource resource = new MainResource();
        resource.setResourceId(id);
        resource.setName("mainresource" + id);
        resource.setRegion(region);
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.EC2.getValue());
        resource.setPlatform(platform);
        MetricType mt1 = TestMetricProvider.createMetricTypeString();
        Metric m1 = TestMetricProvider.createMetric(3L, "instance-type", mt1);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, instanceType);
        resource.setMetricValues(Set.of(mv1));
        return resource;
    }

    public static Resource createResourceOpenFaas(long id, Region region, String gatewayUrl, String openfaasUser,
            String openfaasPw) {
        MainResource resource = new MainResource();
        resource.setResourceId(id);
        resource.setName("mainresource" + id);
        resource.setRegion(region);
        Platform platform = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.OPENFAAS.getValue());
        resource.setPlatform(platform);
        MetricType mt1 = TestMetricProvider.createMetricTypeString();
        Metric m1 = TestMetricProvider.createMetric(1L, "gateway-url", mt1);
        Metric m2 = TestMetricProvider.createMetric(2L, "openfaas-user", mt1);
        Metric m3 = TestMetricProvider.createMetric(3L, "openfaas-pw", mt1);
        MetricValue mv1 = TestMetricProvider.createMetricValue(3L, m1, gatewayUrl);
        MetricValue mv2 = TestMetricProvider.createMetricValue(4L, m2, openfaasUser);
        MetricValue mv3 = TestMetricProvider.createMetricValue(5L, m3, openfaasPw);
        resource.setMetricValues(Set.of(mv1, mv2, mv3));
        return resource;
    }

    public static Resource createResourceOpenFaas(long id, String gatewayUrl, String openfaasUser, String openfaasPw) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        return createResourceOpenFaas(id, region, gatewayUrl, openfaasUser, openfaasPw);
    }


    public static Resource createResourceFaaS(long id, Region region, Double timeout, Double memorySize) {
        MainResource resource = new MainResource();
        resource.setResourceId(id);
        resource.setName("mainresource" + id);
        resource.setRegion(region);
        MetricType mt = TestMetricProvider.createMetricType(1L, "number");
        Metric m1 = TestMetricProvider.createMetric(1L, "timeout", mt);
        Metric m2 = TestMetricProvider.createMetric(2L, "memory-size", mt);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, m1, timeout);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, m2, memorySize);
        resource.setMetricValues(Set.of(mv1, mv2));
        return resource;
    }

    public static Resource createResourceContainer(long id, Region region, String clusterUrl, boolean hasExternalIp) {
        MainResource resource = new MainResource();
        resource.setResourceId(id);
        resource.setName("mainresource" + id);
        resource.setRegion(region);
        Platform platform = TestPlatformProvider.createPlatformContainer(1L, PlatformEnum.K8S.getValue());
        resource.setPlatform(platform);
        MetricType mt1 = TestMetricProvider.createMetricTypeNumber();
        MetricType mt2 = TestMetricProvider.createMetricTypeString();
        Metric m1 = TestMetricProvider.createMetric(1L, "cluster-url", mt2);
        Metric m2 = TestMetricProvider.createMetric(2L, "pre-pull-timeout", mt1);
        Metric m3 = TestMetricProvider.createMetric(3L, "external-ip", mt2);
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

    public static List<ResourceId> createResourceIdsList(long... resourceIds) {
        return Arrays.stream(resourceIds)
            .mapToObj(id -> {
                ResourceId resourceId = new ResourceId();
                resourceId.setResourceId(id);
                return resourceId;
            })
            .collect(Collectors.toList());
    }

    public static ResourceDTO createResourceDTO(Resource resource) {
        return new ResourceDTO(resource);
    }
}
