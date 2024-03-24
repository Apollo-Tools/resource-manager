package at.uibk.dps.rm.entity.dto.deployment;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Represents a DTO that is used for the validation of an active deployment.
 *
 * @author matthi-g
 */
@Data
@RequiredArgsConstructor
public class StartupShutdownServiceDeploymentDTO {
    private Long deploymentId;
    private List<ServiceDeploymentId> serviceDeployments;
    private Boolean ignoreRunningStateChange = false;
}
