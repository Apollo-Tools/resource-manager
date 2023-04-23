package at.uibk.dps.rm.util;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A utility class to map entities to their regions.
 *
 * @author matthi-g
 */
@UtilityClass
public class RegionMapper {

    /**
     * Map and group function resources to their region.
     *
     * @param functionResources the function resource
     * @return the mapped/grouped function resources
     */
    public static Map<Region, List<FunctionResource>> mapFunctionResources(List<FunctionResource> functionResources) {
        return functionResources.stream()
            .collect(Collectors.groupingBy(functionResource -> functionResource.getResource().getRegion()));
    }

    /**
     * Map vpcs to their region.
     *
     * @param  vpcList the list of vpcs
     * @return the mapped vpcs
     */
    public static Map<Region, VPC> mapVPCs( List<VPC> vpcList) {
        return vpcList.stream()
            .collect(Collectors.toMap(VPC::getRegion, vpc -> vpc, (vpc1, vpc2) -> vpc1));
    }
}
