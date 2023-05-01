package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.util.serialization.SLORequestDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonDeserialize(using = SLORequestDeserializer.class)
public abstract class SLORequest {

    @JsonProperty("slos")
    private List<ServiceLevelObjective> serviceLevelObjectives =new ArrayList<>();

    private List<Long> regions = new ArrayList<>();

    private List<Long> providers = new ArrayList<>();

    private List<Long> resourceTypes = new ArrayList<>();
}
