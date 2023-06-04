package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

/**
 * Represents the resource entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    // TODO: remove
    @JsonProperty("is_self_managed")
    private Boolean isSelfManaged = false;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp updatedAt;

    // TODO: remove
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type")
    private ResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @OneToMany
    @JoinColumn(name="resource_id")
    private Set<MetricValue> metricValues;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Resource resource = (Resource) obj;
        return resourceId.equals(resource.resourceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return resourceId.hashCode();
    }
}
