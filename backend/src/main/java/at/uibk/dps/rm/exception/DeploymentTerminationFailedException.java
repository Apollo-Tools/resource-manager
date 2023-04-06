package at.uibk.dps.rm.exception;

public class DeploymentTerminationFailedException extends RuntimeException {
    public DeploymentTerminationFailedException() {
        super("deployment/termination failed");
    }
}
