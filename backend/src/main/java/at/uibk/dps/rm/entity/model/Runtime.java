package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the runtime entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Runtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long runtimeId;

    private String name;

    private String templatePath = "";

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE)
    Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Runtime runtime = (Runtime) obj;
        return runtimeId.equals(runtime.runtimeId);
    }

    @Override
    @Generated
    public int hashCode() {
        return runtimeId.hashCode();
    }
}
