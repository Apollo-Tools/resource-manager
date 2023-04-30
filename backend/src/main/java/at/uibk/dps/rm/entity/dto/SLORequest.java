package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class SLORequest {

    @JsonProperty("slos")
    private List<ServiceLevelObjective> serviceLevelObjectives =new ArrayList<>();

    private List<Long> regions = new ArrayList<>();

    private List<Long> providers = new ArrayList<>();

    private List<Long> resourceTypes = new ArrayList<>();
}
