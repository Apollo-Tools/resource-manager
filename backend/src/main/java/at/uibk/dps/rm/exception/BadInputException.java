package at.uibk.dps.rm.exception;

import io.vertx.serviceproxy.ServiceException;

/**
 * The BadInputException indicates that some input values violate preconditions.
 *
 * @author matthi-g
 */
public class BadInputException extends ServiceException {

    private static final long serialVersionUID = -6403666897011374139L;

    /**
     * Create an instance with the message "bad input".
     */
    public BadInputException() {
        this("bad input");
    }

    /**
     * Create an instance with the message.
     */
    public BadInputException(String message) {
        super(400, message);
    }

    /**
     * Create an instance from an existing BadInputException.
     */
    public BadInputException(BadInputException badInputException) {
        this(badInputException.getMessage());
    }
}
