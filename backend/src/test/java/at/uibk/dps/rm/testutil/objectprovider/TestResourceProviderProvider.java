package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.*;
import lombok.experimental.UtilityClass;

/**
 * Utility class to instantiate objects that are linked to the resource_provider entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestResourceProviderProvider {
    public static ResourceProvider createResourceProvider(long providerId) {
        ResourceProvider resourceProvider = new ResourceProvider();
        resourceProvider.setProviderId(providerId);
        resourceProvider.setProvider("aws");
        return resourceProvider;
    }

    public static ResourceProvider createResourceProvider(long providerId, String provider) {
        ResourceProvider resourceProvider = createResourceProvider(providerId);
        resourceProvider.setProvider(provider);
        return resourceProvider;
    }

    public static ResourceProvider createResourceProvider(long providerId, String provider, Environment environment) {
        ResourceProvider resourceProvider = createResourceProvider(providerId);
        resourceProvider.setEnvironment(environment);
        resourceProvider.setProvider(provider);
        return resourceProvider;
    }

    public static Region createRegion(long id, String name) {
        ResourceProvider rp = createResourceProvider(1L, "aws");
        return createRegion(id, name, rp);
    }

    public static Region createRegion(long id, String name, ResourceProvider resourceProvider) {
        Region region = new Region();
        region.setRegionId(id);
        region.setName(name);
        region.setResourceProvider(resourceProvider);
        return region;
    }

    public static VPC createVPC(long id, Region region, String vpcIdValue, String subnetIdValue, Account createdBy) {
        VPC vpc = new VPC();
        vpc.setVpcId(id);
        vpc.setRegion(region);
        vpc.setVpcIdValue(vpcIdValue);
        vpc.setSubnetIdValue(subnetIdValue);
        vpc.setCreatedBy(createdBy);
        return vpc;
    }

    public static VPC createVPC(long id, Region region) {
        Account account = TestAccountProvider.createAccount(1L);
        return createVPC(id, region, "vpc-id", "subnet-id", account);
    }

    public static VPC createVPC(long id, Region region, Account createdBy) {
        return createVPC(id, region, "vpc-id", "subnet-id", createdBy);
    }

    public static K8sNamespace createNamespace(long id, String name, Resource resource) {
        K8sNamespace namespace = new K8sNamespace();
        namespace.setNamespaceId(id);
        namespace.setResource(resource);
        namespace.setNamespace(name);
        return namespace;
    }

    public static K8sNamespace createNamespace(long id, Resource resource) {
        return createNamespace(id, "namespace", resource);
    }

    public static K8sNamespace createNamespace(long id) {
        Resource resource = TestResourceProvider.createResource(id + 1);
        return createNamespace(id, resource);
    }

    public static Environment createEnvironment(long id, String name) {
        Environment environment = new Environment();
        environment.setEnvironmentId(id);
        environment.setEnvironment(name);
        return environment;
    }
}
