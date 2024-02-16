package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the resource_deployment entity.
 *
 * @author matthi-g
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "deployment_type")
@Getter
@Setter
public abstract class ResourceDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceDeploymentId;

    private String rmTriggerUrl = "";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id")
    private Deployment deployment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_status_id")
    private ResourceDeploymentStatus status;

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
        ResourceDeployment that = (ResourceDeployment) obj;
        return resourceDeploymentId.equals(that.resourceDeploymentId);
    }

    @Override
    @Generated
    public int hashCode() {
        return resourceDeploymentId.hashCode();
    }
}
