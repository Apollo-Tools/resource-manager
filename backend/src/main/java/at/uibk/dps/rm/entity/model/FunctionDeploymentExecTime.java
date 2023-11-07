package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents the function deployment exec time entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class FunctionDeploymentExecTime implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long execTimeId;

    @Id
    private Timestamp time;

    private Integer execTimeMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_deployment_id")
    private FunctionDeployment functionDeployment;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FunctionDeploymentExecTime that = (FunctionDeploymentExecTime) obj;
        return execTimeId.equals(that.execTimeId);
    }

    @Override
    @Generated
    public int hashCode() {
        return execTimeId.hashCode();
    }
}
