package at.uibk.dps.rm.entity.dto.deployment;

import lombok.Data;

/**
 * Represents the id of a service deployment.
 *
 * @author matthi-g
 */
@Data
public class ServiceDeploymentId {
    private long resourceDeploymentId;
}
