package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.model.FunctionDeployment;
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
     * Map and group function deployments by their region.
     *
     * @param functionDeployments the function deployments
     * @return the mapped/grouped function deployments
     */
    public static Map<Region, List<FunctionDeployment>> mapFunctionDeployments(List<FunctionDeployment>
            functionDeployments) {
        return functionDeployments.stream()
            .collect(Collectors.groupingBy(functionDeployment ->
                functionDeployment.getResource().getMain().getRegion()));
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
