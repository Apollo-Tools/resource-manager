package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.util.ListResourcesBySLOsRequestDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the body of the listResourcesBySLOs operation.
 *
 * @author matthi-g
 */
@Data
@JsonDeserialize(using = ListResourcesBySLOsRequestDeserializer.class)
public class ListResourcesBySLOsRequest {

    @JsonProperty("slos")
    private List<ServiceLevelObjective> serviceLevelObjectives =new ArrayList<>();
    //TODO: remove
    private List<Long> regions = new ArrayList<>();

    private List<Long> providers = new ArrayList<>();

    private List<Long> resourceTypes = new ArrayList<>();
}
