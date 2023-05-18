package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the resource_ensemble entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class ResourceEnsemble {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceEnsembleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ensemble_id")
    private Ensemble ensemble;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

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
        ResourceEnsemble that = (ResourceEnsemble) obj;
        return resourceEnsembleId.equals(that.resourceEnsembleId);
    }

    @Override
    @Generated
    public int hashCode() {
        return resourceEnsembleId.hashCode();
    }
}
