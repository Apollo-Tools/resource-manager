package at.uibk.dps.rm.entity.dto.resource;

import at.uibk.dps.rm.entity.model.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;

/**
 * Represents a sub resource which is used for the de-/serialization of the
 * {@link at.uibk.dps.rm.entity.model.SubResource} class
 *
 * @author matthi-g
 */
@Getter
@Setter
public class SubResourceDTO extends Resource {

    /**
     * Create an instance.
     */
    public SubResourceDTO() {}

    /**
     * Create an instance from the subResource.
     *
     * @param subResource the sub resource
     */
    public SubResourceDTO(SubResource subResource) {
        MainResource mainResource = subResource.getMain();
        this.setResourceId(subResource.getResourceId());
        this.setName(subResource.getName());
        this.setMainResourceId(mainResource.getResourceId());
        this.setPlatform(mainResource.getPlatform());
        this.setRegion(mainResource.getRegion());
        this.setCreatedAt(subResource.getCreatedAt());
        this.setUpdatedAt(subResource.getUpdatedAt());
        this.setMetricValues(subResource.getMetricValues());
        this.setMainMetricValues(mainResource.getMetricValues());
    }

    private long mainResourceId;

    private Region region;

    private Platform platform;

    private Set<MetricValue> mainMetricValues;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
