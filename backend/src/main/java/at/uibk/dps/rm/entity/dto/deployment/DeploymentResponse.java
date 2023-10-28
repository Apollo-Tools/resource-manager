package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Represents one entry in the listMyDeployments operation.
 *
 * @author matthi-g
 */
@Data
public class DeploymentResponse {

    public long deploymentId;

    public DeploymentStatusValue statusValue;

    private Timestamp createdAt;

    private Timestamp finishedAt;
}
