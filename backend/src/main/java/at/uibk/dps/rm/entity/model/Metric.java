package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the metric entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    @SuppressWarnings("PMD")
    private String metric;

    private String description;

    @ManyToOne
    @JoinColumn(name="metric_type_id", nullable = false)
    private MetricType metricType;

    @Column(insertable = false, updatable = false)
    private  @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Metric metric = (Metric) obj;
        return metricId.equals(metric.metricId);
    }

    @Override
    @Generated
    public int hashCode() {
        return metricId.hashCode();
    }
}
