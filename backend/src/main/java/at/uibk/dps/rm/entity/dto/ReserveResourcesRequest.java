package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import lombok.Data;

import java.util.List;

@Data
public class ReserveResourcesRequest {
    private List<FunctionResourceIds> functionResources;
}
