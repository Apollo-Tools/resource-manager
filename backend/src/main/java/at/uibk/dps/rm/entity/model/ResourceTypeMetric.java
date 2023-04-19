package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the resource_type_metric entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class ResourceTypeMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceTypeMetricId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id")
    private ResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id")
    private Metric metric;

    private Boolean required;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ResourceTypeMetric rtm = (ResourceTypeMetric) obj;
        return resourceTypeMetricId.equals(rtm.resourceTypeMetricId);
    }

    @Override
    @Generated
    public int hashCode() {
        return resourceTypeMetricId.hashCode();
    }
}
