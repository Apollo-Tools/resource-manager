package at.uibk.dps.rm.util;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegionMapper {

    public static Map<Region, List<FunctionResource>> mapFunctionResources(List<FunctionResource> functionResources) {
        return functionResources.stream()
            .collect(Collectors.groupingBy(functionResource -> functionResource.getResource().getRegion()));
    }

    public static Map<Region, VPC> mapVPCs(List<VPC> vpcList) {
        return vpcList.stream()
            .collect(Collectors.toMap(VPC::getRegion, vpc -> vpc, (vpc1, vpc2) -> vpc1));
    }
}
