package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the deployment entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deploymentId;

    @Column(columnDefinition="TEXT")
    private String alertNotificationUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ensemble_id")
    private Ensemble ensemble;

    @OneToMany(mappedBy = "deployment", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<FunctionDeployment> functionDeployments = new ArrayList<>();

    @OneToMany(mappedBy = "deployment", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<ServiceDeployment> serviceDeployments = new ArrayList<>();

    @OneToMany(mappedBy = "lockedByDeployment", cascade = CascadeType.ALL)
    private List<Resource> lockedResources = new ArrayList<>();

    private Boolean containerStateChange = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Account createdBy;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp finishedAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Deployment that = (Deployment) obj;
        return deploymentId.equals(that.deploymentId);
    }

    @Override
    @Generated
    public int hashCode() {
        return deploymentId.hashCode();
    }
}
