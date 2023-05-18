package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the service entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

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
        Service that = (Service) obj;
        return serviceId.equals(that.serviceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return serviceId.hashCode();
    }
}
