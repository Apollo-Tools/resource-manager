package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.util.serialization.SLORequestDeserializer;
import at.uibk.dps.rm.util.serialization.SLORequestSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.Generated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Superclass of requests that contain service level objectives.
 *
 * @author matthi-g
 */
@Data
@JsonDeserialize(using = SLORequestDeserializer.class)
@JsonSerialize(using = SLORequestSerializer.class)
public abstract class SLORequest {

    @JsonProperty("slos")
    private List<ServiceLevelObjective> serviceLevelObjectives =new ArrayList<>();

    private List<Long> environments = new ArrayList<>();
    private List<Long> resourceTypes = new ArrayList<>();
    private List<Long> platforms = new ArrayList<>();
    private List<Long> regions = new ArrayList<>();
    private List<Long> providers = new ArrayList<>();

    @Generated
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        SLORequest that = (SLORequest) object;

        if (!Objects.equals(serviceLevelObjectives, that.serviceLevelObjectives))
            return false;
        if (!Objects.equals(environments, that.environments)) return false;
        if (!Objects.equals(resourceTypes, that.resourceTypes))
            return false;
        if (!Objects.equals(platforms, that.platforms)) return false;
        if (!Objects.equals(regions, that.regions)) return false;
        return Objects.equals(providers, that.providers);
    }

    @Generated
    @Override
    public int hashCode() {
        int result = serviceLevelObjectives != null ? serviceLevelObjectives.hashCode() : 0;
        result = 31 * result + (environments != null ? environments.hashCode() : 0);
        result = 31 * result + (resourceTypes != null ? resourceTypes.hashCode() : 0);
        result = 31 * result + (platforms != null ? platforms.hashCode() : 0);
        result = 31 * result + (regions != null ? regions.hashCode() : 0);
        result = 31 * result + (providers != null ? providers.hashCode() : 0);
        return result;
    }
}
