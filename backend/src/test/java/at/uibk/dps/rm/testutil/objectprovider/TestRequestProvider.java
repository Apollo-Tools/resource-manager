package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.GetResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;

import java.util.List;

public class TestRequestProvider {
    public static GetResourcesBySLOsRequest createResourceBySLOsRequest(List<ServiceLevelObjective> slos, int limit,
                                                                        List<Long> providers,
                                                                        List<Long> resourceTypes,
                                                                        List<String> regions) {
        GetResourcesBySLOsRequest request = new GetResourcesBySLOsRequest();
        request.setServiceLevelObjectives(slos);
        request.setLimit(limit);
        request.setProviders(providers);
        request.setRegions(regions);
        request.setResourceTypes(resourceTypes);
        return request;
    }

    public static ReserveResourcesRequest createReserveResourcesRequest(List<FunctionResourceIds> functionResources,
                                                                        DockerCredentials dockerCredentials) {
        ReserveResourcesRequest request = new ReserveResourcesRequest();
        request.setFunctionResources(functionResources);
        request.setDockerCredentials(dockerCredentials);
        return request;
    }

    public static ReserveResourcesRequest createReserveResourcesRequest(List<FunctionResourceIds> functionResources) {
        return createReserveResourcesRequest(functionResources, TestDTOProvider.createDockerCredentials());
    }
}
