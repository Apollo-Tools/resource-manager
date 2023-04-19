package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the resource_type entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class ResourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long typeId;

    @SuppressWarnings("PMD")
    private String resourceType;

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
        ResourceType that = (ResourceType) obj;
        return typeId.equals(that.typeId);
    }

    @Override
    @Generated
    public int hashCode() {
        return typeId.hashCode();
    }
}
