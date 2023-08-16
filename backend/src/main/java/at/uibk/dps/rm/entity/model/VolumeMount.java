package at.uibk.dps.rm.entity.model;


import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Represents the volume_mount entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class VolumeMount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long volumeMountId;

    private String name;

    private String mountPath;

    @Column(precision = 16, scale = 6)
    private BigDecimal sizeMegabytes;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE)
    Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VolumeMount volumeMount = (VolumeMount) obj;
        return name.equals(volumeMount.name);
    }

    @Override
    @Generated
    public int hashCode() {
        return name.hashCode();
    }
}
