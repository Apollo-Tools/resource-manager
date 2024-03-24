package at.uibk.dps.rm.exception;

import io.vertx.serviceproxy.ServiceException;

/**
 * The ConflictException indicates that there is a state conflict.
 *
 * @author matthi-g
 */
public class ConflictException extends ServiceException {

    private static final long serialVersionUID = -6403666897011374139L;

    /**
     * Create an instance with the message.
     */
    public ConflictException(String message) {
        super(409, message);
    }

    /**
     * Create an instance from an existing ConflictException.
     */
    public ConflictException(ConflictException conflictException) {
        this(conflictException.getMessage());
    }
}
