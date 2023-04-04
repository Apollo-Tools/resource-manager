package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.entity.model.VPC;

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

    public static VPC createVPC(long id, Account createdBy) {
        ResourceProvider resourceProvider = new ResourceProvider();
        resourceProvider.setProviderId(1L);
        resourceProvider.setProvider("aws");
        Region region = createRegion(1L, "us-east-1", resourceProvider);
        return createVPC(id, region, "vpc-id", "subnet-id", createdBy);
    }

    public static VPC createVPC(long id, Region region) {
        Account account = TestAccountProvider.createAccount(1L);
        return createVPC(id, region, "vpc-id", "subnet-id", account);
    }

    public static VPC createVPC(long id, Region region, Account createdBy) {
        return createVPC(id, region, "vpc-id", "subnet-id", createdBy);
    }
}
