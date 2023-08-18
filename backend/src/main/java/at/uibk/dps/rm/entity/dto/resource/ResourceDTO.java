package at.uibk.dps.rm.entity.dto.resource;

import at.uibk.dps.rm.entity.model.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

/**
 * Represents a resource which is used for the de-/serialization of the
 * {@link at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble} class
 *
 * @author matthi-g
 */
@Getter
@Setter
public class ResourceDTO  extends Resource {

    /**
     * Create an instance.
     */
    public ResourceDTO() {}

    /**
     * Create an instance from an existing resource.
     *
     * @param resource the resource
     */
    public ResourceDTO(Resource resource) {
        MainResource mainResource = resource.getMain();
        this.setResourceId(resource.getResourceId());
        this.setName(resource.getName());
        this.setMainResourceId(mainResource.getResourceId());
        this.setPlatform(mainResource.getPlatform());
        this.setRegion(mainResource.getRegion());
        this.setCreatedAt(resource.getCreatedAt());
        this.setUpdatedAt(resource.getUpdatedAt());
        this.setSubResources(this.getResourceId() == this.mainResourceId ? mainResource.getSubResources() : List.of());
        this.setMetricValues(resource.getMetricValues());
    }

    private long mainResourceId;

    private Region region;

    private Platform platform;

    private List<SubResource> subResources;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
