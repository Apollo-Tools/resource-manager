package at.uibk.dps.rm.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReserveResourcesRequest {

    private List<Long> resources;

    private boolean deployResources;
}
