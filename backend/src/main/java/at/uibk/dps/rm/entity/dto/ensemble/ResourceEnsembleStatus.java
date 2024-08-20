package at.uibk.dps.rm.entity.dto.ensemble;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.beans.ConstructorProperties;

/**
 * Represents one entry in the validateEnsemble response
 *
 * @author matthi-g
 */
@Data
public class ResourceEnsembleStatus {

    /**
     * Create an instance from the resourceId and isValid.
     *
     * @param resourceId the resource id
     * @param isValid the validity of the resource
     */
    @ConstructorProperties({"resource_id", "is_valid"})
    public ResourceEnsembleStatus(long resourceId, boolean isValid) {
        this.resourceId = resourceId;
        this.isValid = isValid;
    }

    @JsonProperty("resource_id")
    private final long resourceId;

    @JsonProperty("is_valid")
    private final boolean isValid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceEnsembleStatus that = (ResourceEnsembleStatus) o;

        if (resourceId != that.resourceId) return false;
        return isValid == that.isValid;
    }

    @Override
    public int hashCode() {
        int result = (int) (resourceId ^ (resourceId >>> 32));
        result = 31 * result + (isValid ? 1 : 0);
        return result;
    }
}
