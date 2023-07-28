package at.uibk.dps.rm.entity.dto.resource;

import at.uibk.dps.rm.entity.model.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
public class SubResourceDTO extends Resource {

    public SubResourceDTO() {}

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
