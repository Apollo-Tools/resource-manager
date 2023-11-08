package at.uibk.dps.rm.entity.model;

import lombok.AccessLevel;
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
    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Long execTimeId;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp time;

    @Column(name = "exec_time_ms")
    private Integer execTimeMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_deployment_id")
    private FunctionDeployment functionDeployment;
}
