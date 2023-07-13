package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link RegionMapper} class.
 *
 * @author matthi-g
 */
public class RegionMapperTest {

    @Test
    void mapFunctionResources() {
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Region region2 = TestResourceProviderProvider.createRegion(2L, "us-east-2");
        Region region3 = TestResourceProviderProvider.createRegion(3L, "custom-edge");
        Resource r1 = TestResourceProvider.createResourceLambda(1L, region1, 20.0, 20.0);
        Resource r2 = TestResourceProvider.createResourceLambda(1L, region2, 20.0, 20.0);
        Resource r3 = TestResourceProvider.createResourceOpenFaas(1L, region3, 20.0, 20.0,
            "localhost", "admin", "pw");
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionDeployment(1L, r1);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionDeployment(2L, r1);
        FunctionDeployment fr3 = TestFunctionProvider.createFunctionDeployment(3L, r2);
        FunctionDeployment fr4 = TestFunctionProvider.createFunctionDeployment(4L, r3);

        Map<Region, List<FunctionDeployment>> result = RegionMapper
            .mapFunctionDeployments(List.of(fr1, fr2, fr3, fr4));

        assertThat(result.get(region1).size()).isEqualTo(2);
        assertThat(result.get(region1).get(0).getResourceDeploymentId()).isEqualTo(1L);
        assertThat(result.get(region1).get(1).getResourceDeploymentId()).isEqualTo(2L);
        assertThat(result.get(region2).size()).isEqualTo(1);
        assertThat(result.get(region2).get(0).getResourceDeploymentId()).isEqualTo(3L);
        assertThat(result.get(region3).size()).isEqualTo(1);
        assertThat(result.get(region3).get(0).getResourceDeploymentId()).isEqualTo(4L);
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
