package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the status of resource deployments.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum DeploymentStatusValue {
    /**
     * A deployment is new and about to be deployed
     */
    NEW("NEW"),
    /**
     * An error occured during deployment/termination
     */
    ERROR("ERROR"),
    /**
     * The resource is deployed and ready
     */
    DEPLOYED("DEPLOYED"),
    /**
     * Resource is in the process of being terminated
     */
    TERMINATING("TERMINATING"),
    /**
     * The resource is terminated
     */
    TERMINATED("TERMINATED");

    private final String value;

    /**
     * Create an instance from a ResourceDeploymentStatus. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param deploymentStatus the deployment status
     * @return the created object
     */
    public static DeploymentStatusValue fromDeploymentStatus(ResourceDeploymentStatus deploymentStatus) {
        return Arrays.stream(DeploymentStatusValue.values())
            .filter(value -> value.value.equals(deploymentStatus.getStatusValue()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown value: " + deploymentStatus.getStatusValue()));
    }
}
