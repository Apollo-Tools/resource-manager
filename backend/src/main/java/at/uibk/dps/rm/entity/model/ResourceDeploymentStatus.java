package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the resource_deployment_status entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class ResourceDeploymentStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusId;

    private String statusValue;

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
        ResourceDeploymentStatus status = (ResourceDeploymentStatus) obj;
        return statusId.equals(status.statusId);
    }

    @Override
    @Generated
    public int hashCode() {
        return statusId.hashCode();
    }
}
