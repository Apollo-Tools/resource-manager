package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.model.FunctionReservation;
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
     * Map and group function reservations to their region.
     *
     * @param functionReservations the function reservations
     * @return the mapped/grouped function reservations
     */
    public static Map<Region, List<FunctionReservation>> mapFunctionReservations(List<FunctionReservation>
            functionReservations) {
        return functionReservations.stream()
            .collect(Collectors.groupingBy(functionReservation -> functionReservation.getResource().getRegion()));
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
