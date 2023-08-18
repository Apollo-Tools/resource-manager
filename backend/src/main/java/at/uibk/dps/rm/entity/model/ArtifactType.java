package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the artifact type entity.
 *
 * @author matthi-g
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "artifact")
@Getter
@Setter
public abstract class ArtifactType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artifactTypeId;

    private String name;

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
        ArtifactType artifactType = (ArtifactType) obj;
        return artifactTypeId.equals(artifactType.artifactTypeId);
    }

    @Override
    @Generated
    public int hashCode() {
        return artifactTypeId.hashCode();
    }
}
