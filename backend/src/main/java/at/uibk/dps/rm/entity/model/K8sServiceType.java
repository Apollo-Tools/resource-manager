package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the service_type entity.
 *
 * @author matthi-g
 */
@Entity(name="K8sServiceType")
@Table(name="service_type")
@Getter
@Setter
public class K8sServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceTypeId;

    private String name;

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
        K8sServiceType that = (K8sServiceType) obj;
        return serviceTypeId.equals(that.serviceTypeId);
    }

    @Override
    @Generated
    public int hashCode() {
        return serviceTypeId.hashCode();
    }
}
