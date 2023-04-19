package at.uibk.dps.rm.exception;

/**
 * The DeploymentTerminationFailedException indicates that a searched entity could not be found.
 *
 * @author matthi-g
 */
public class NotFoundException extends RuntimeException{

    private static final long serialVersionUID = 3390254567609272315L;

    /**
     * Create an instance with the message "not found".
     */
    public NotFoundException() {
        super("not found");
    }
}
