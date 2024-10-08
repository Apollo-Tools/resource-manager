package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import com.fasterxml.jackson.annotation.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the resource entity.
 *
 * @author matthi-g
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "resource_type")
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MainResource.class, name = "MainResource"),
    @JsonSubTypes.Type(value = SubResource.class, name = "SubResource"),
    @JsonSubTypes.Type(value = SubResourceDTO.class, name = "SubResourceDTO")
})
public abstract class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    private String name;

    private Boolean isLockable;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "locked_by_deployment")
    private Deployment lockedByDeployment;

    @Transient
    @JsonProperty("is_locked")
    private Boolean isLocked;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp updatedAt;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "resource")
    private Set<MetricValue> metricValues;

    @Transient
    private Set<MonitoredMetricValue> monitoredMetricValues = new HashSet<>();

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

    /**
     * Get the main resource of this resource.
     *
     * @return the main resource if the resource is a sub resource, else this resource
     */
    @JsonIgnore
    public MainResource getMain() {
        if (this instanceof MainResource) {
            return (MainResource) this;
        }
        SubResource subResource = (SubResource) this;
        return subResource.getMainResource();
    }

    @Override
    public String toString() {
        return  this.getName() + "(" + this.getResourceId() + ")";
    }
}
