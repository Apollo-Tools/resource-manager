package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.credentials.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import lombok.Data;

import java.util.List;

/**
 * Represents the body of the deployResources operation.
 *
 * @author matthi-g
 */
@Data
public class DeployResourcesRequest {
    private List<FunctionResourceIds> functionResources;

    private List<ServiceResourceIds> serviceResources;

    private List<ResourceId> lockResources;

    private DeploymentCredentials credentials = new DeploymentCredentials();
}
