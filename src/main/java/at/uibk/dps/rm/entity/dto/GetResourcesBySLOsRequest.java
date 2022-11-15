package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class GetResourcesBySLOsRequest {

    private final @Getter(value = AccessLevel.NONE) @Setter(value = AccessLevel.NONE) int DEFAULT_LIMIT = 10000;

    @JsonProperty("slo")
    private List<ServiceLevelObjective> serviceLevelObjectives;

    private int limit = DEFAULT_LIMIT;
}
