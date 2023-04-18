package at.uibk.dps.rm.exception;

public class DeploymentTerminationFailedException extends RuntimeException {

    private static final long serialVersionUID = 6852653163033500810L;

    public DeploymentTerminationFailedException() {
        super("deployment/termination failed");
    }
}
