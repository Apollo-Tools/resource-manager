package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the platform_metric entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class PlatformMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long platformMetricId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id")
    private Metric metric;

    private Boolean isMainResourceMetric;

    private Boolean isSubResourceMetric;

    private Boolean monitorDuringDeployment;

    @JsonProperty("is_monitored")
    private Boolean isMonitored;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PlatformMetric pm = (PlatformMetric) obj;
        return platformMetricId.equals(pm.platformMetricId);
    }

    @Override
    @Generated
    public int hashCode() {
        return platformMetricId.hashCode();
    }
}
