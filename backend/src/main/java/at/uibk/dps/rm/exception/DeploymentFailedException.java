package at.uibk.dps.rm.exception;

public class DeploymentFailedException extends RuntimeException {
    public DeploymentFailedException() {
        super("deployment failed");
    }
}
