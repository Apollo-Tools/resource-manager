package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the body of the listResourcesBySLOs operation.
 *
 * @author matthi-g
 */
@Data
public class ListResourcesBySLOsRequest {

    @JsonProperty("slo")
    private List<ServiceLevelObjective> serviceLevelObjectives =new ArrayList<>();
    //TODO: remove
    private List<String> regions = new ArrayList<>();

    private List<Long> providers = new ArrayList<>();

    private List<Long> resourceTypes = new ArrayList<>();

    private int limit = 10000;
}
