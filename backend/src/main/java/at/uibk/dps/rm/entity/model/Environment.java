package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the environment entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Environment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long environmentId;

    private String environment;

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
        Environment environment = (Environment) obj;
        return environmentId.equals(environment.environmentId);
    }

    @Override
    @Generated
    public int hashCode() {
        return environmentId.hashCode();
    }
}
