package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class GetResourcesBySLOsRequest {
    private final int DEFAULT_LIMIT = 10000;

    @JsonAlias("slo")
    private List<ServiceLevelObjective> serviceLevelObjectives;

    private int limit = DEFAULT_LIMIT;

    public GetResourcesBySLOsRequest() {
    }

    public List<ServiceLevelObjective> getServiceLevelObjectives() {
        return serviceLevelObjectives;
    }

    public void setServiceLevelObjectives(List<ServiceLevelObjective> serviceLevelObjectives) {
        this.serviceLevelObjectives = serviceLevelObjectives;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
