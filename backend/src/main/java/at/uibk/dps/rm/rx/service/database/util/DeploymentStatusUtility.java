package at.uibk.dps.rm.rx.service.database.util;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class DeploymentStatusUtility {
    /**
     * Get the crucial resource deployment status based on all resource deployments of a single
     * deployment.
     *
     * @param resourceDeployments the resource deployments
     * @return the crucial deployment status
     */
    public static DeploymentStatusValue checkCrucialResourceDeploymentStatus(List<ResourceDeployment> resourceDeployments) {
        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.ERROR)) {
            return DeploymentStatusValue.ERROR;
        }
        if (matchAnyResourceDeploymentsStatus(resourceDeployments, DeploymentStatusValue.NEW)) {
            return DeploymentStatusValue.NEW;
        }

        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.TERMINATING)) {
            return DeploymentStatusValue.TERMINATING;
        }

        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.DEPLOYED)) {
            return DeploymentStatusValue.DEPLOYED;
        }
        return DeploymentStatusValue.TERMINATED;
    }

    /**
     * Check if at least one status of resourceDeployments matches the given status value.
     *
     * @param resourceDeployments the resource deployments
     * @param statusValue the status value
     * @return true if at least one match was found, else false
     */
    private static boolean matchAnyResourceDeploymentsStatus(List<ResourceDeployment> resourceDeployments,
                                                      DeploymentStatusValue statusValue) {
        return resourceDeployments.stream()
            .anyMatch(rd -> DeploymentStatusValue.fromDeploymentStatus(rd.getStatus()).equals(statusValue));
    }
}
