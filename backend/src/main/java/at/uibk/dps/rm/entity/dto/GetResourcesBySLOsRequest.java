package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetResourcesBySLOsRequest {

    private final static @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) int DEFAULT_LIMIT = 10_000;

    @JsonProperty("slo")
    private List<ServiceLevelObjective> serviceLevelObjectives =new ArrayList<>();

    private List<String> regions = new ArrayList<>();

    private List<Long> providers = new ArrayList<>();

    private List<Long> resourceTypes = new ArrayList<>();

    private int limit = DEFAULT_LIMIT;
}
