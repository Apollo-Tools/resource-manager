package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateEnsembleRequest {

    private String name;

    @JsonProperty("slos")
    private List<ServiceLevelObjective> serviceLevelObjectives =new ArrayList<>();

    private List<ResourceId> resources = new ArrayList<>();


}
