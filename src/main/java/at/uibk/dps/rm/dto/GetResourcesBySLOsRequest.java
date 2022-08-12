package at.uibk.dps.rm.dto;

import at.uibk.dps.rm.dto.slo.ServiceLevelObjective;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class GetResourcesBySLOsRequest {
    @JsonAlias("slo")
    private List<ServiceLevelObjective> serviceLevelObjectives;

    private long limit;

    public GetResourcesBySLOsRequest() {
    }

    public List<ServiceLevelObjective> getServiceLevelObjectives() {
        return serviceLevelObjectives;
    }

    public void setServiceLevelObjectives(List<ServiceLevelObjective> serviceLevelObjectives) {
        this.serviceLevelObjectives = serviceLevelObjectives;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
