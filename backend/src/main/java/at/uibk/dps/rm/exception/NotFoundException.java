package at.uibk.dps.rm.exception;

import io.vertx.serviceproxy.ServiceException;

/**
 * The DeploymentTerminationFailedException indicates that a searched entity could not be found.
 *
 * @author matthi-g
 */
public class NotFoundException extends ServiceException {

    private static final long serialVersionUID = 3390254567609272315L;

    /**
     * Create an instance with the message "not found".
     */
    public NotFoundException() {
        super(404, "not found");
    }

    /**
     * Create an instance with the message.
     */
    public NotFoundException(String message) {
        super(404, message);
    }

    /**
     * Create an instance from the entity class.
     */
    public NotFoundException(Class<?> entityClass) {
        super(404, entityClass.getSimpleName() + " not found");
    }

    /**
     * Create an instance from an existing NotFoundException.
     */
    public NotFoundException(NotFoundException notFoundException) {
        super(404, notFoundException.getMessage());
    }
}
