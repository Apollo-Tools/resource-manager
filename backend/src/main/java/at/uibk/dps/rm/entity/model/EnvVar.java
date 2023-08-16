package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the env_var entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class EnvVar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long envVarId;

    private String name;

    private String value;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

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
        EnvVar envVar = (EnvVar) obj;
        return name.equals(envVar.name);
    }

    @Override
    @Generated
    public int hashCode() {
        return name.hashCode();
    }
}
