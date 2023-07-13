package at.uibk.dps.rm.exception;

/**
 * The DeploymentTerminationFailedException indicates that the deployment or termination of a
 * resource deployment failed.
 *
 * @author matthi-g
 */
public class DeploymentTerminationFailedException extends RuntimeException {

    private static final long serialVersionUID = 6852653163033500810L;

    /**
     * Create an instance with the message "deployment/termination failed".
     */
    public DeploymentTerminationFailedException() {
        super("deployment/termination failed");
    }
}
