package at.uibk.dps.rm.util;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.misc.RegionMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegionMapperTest {

    @Test
    void mapFunctionResources() {
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Region region2 = TestResourceProviderProvider.createRegion(2L, "us-east-2");
        Region region3 = TestResourceProviderProvider.createRegion(3L, "edge");
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region1, 20.0, 20.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region2, "t2.micro");
        Resource r3 = TestResourceProvider.createResource(3L, TestResourceProvider
                .createResourceType(3L, "edge"), region3, false);
        FunctionResource fr1 = TestFunctionProvider.createFunctionResource(1L, r1);
        FunctionResource fr2 = TestFunctionProvider.createFunctionResource(2L, r1);
        FunctionResource fr3 = TestFunctionProvider.createFunctionResource(3L, r2);
        FunctionResource fr4 = TestFunctionProvider.createFunctionResource(4L, r3);

        Map<Region, List<FunctionResource>> result = RegionMapper.mapFunctionResources(List.of(fr1, fr2, fr3, fr4));

        assertThat(result.get(region1).size()).isEqualTo(2);
        assertThat(result.get(region1).get(0).getFunctionResourceId()).isEqualTo(1L);
        assertThat(result.get(region1).get(1).getFunctionResourceId()).isEqualTo(2L);
        assertThat(result.get(region2).size()).isEqualTo(1);
        assertThat(result.get(region2).get(0).getFunctionResourceId()).isEqualTo(3L);
        assertThat(result.get(region3).size()).isEqualTo(1);
        assertThat(result.get(region3).get(0).getFunctionResourceId()).isEqualTo(4L);
    }

    @Test
    void mapVPC() {
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Region region2 = TestResourceProviderProvider.createRegion(2L, "us-east-2");
        VPC vpc1 = TestResourceProviderProvider.createVPC(1L, region1);
        VPC vpc2 = TestResourceProviderProvider.createVPC(2L, region2);
        VPC vpc3 = TestResourceProviderProvider.createVPC(3L, region2);

        Map<Region, VPC> result = RegionMapper.mapVPCs(List.of(vpc1, vpc2, vpc3));

        assertThat(result.get(region1).getVpcId()).isEqualTo(1L);
        assertThat(result.get(region2).getVpcId()).isEqualTo(2L);
    }
}
