package at.uibk.dps.rm.exception;

import io.vertx.serviceproxy.ServiceException;

/**
 * The AlreadyExistsException indicates that an entity that should be stored already exists
 * or that a unique constraint would be violated with the store of the entity.
 *
 * @author matthi-g
 */
public class AlreadyExistsException extends ServiceException {

    private static final long serialVersionUID = -6403666897011374139L;

    /**
     * Create an instance with the message "already exists".
     */
    public AlreadyExistsException() {
        this("already exists");
    }

    /**
     * Create an instance from the entity class.
     */
    public AlreadyExistsException(Class<?> entityClass) {
        this(entityClass.getSimpleName() + " already exists");
    }

    /**
     * Create an instance with the message.
     */
    public AlreadyExistsException(String message) {
        super(409, message);
    }

    /**
     * Create an instance from an existing AlreadyExistsException.
     */
    public AlreadyExistsException(AlreadyExistsException alreadyExistsException) {
        this(alreadyExistsException.getMessage());
    }
}
