package at.uibk.dps.rm.exception;

/**
 * The BadInputException indicates that some input values violate preconditions.
 *
 * @author matthi-g
 */
public class BadInputException extends RuntimeException {

    private static final long serialVersionUID = -6403666897011374139L;

    /**
     * Create an instance with the message "bad input".
     */
    public BadInputException() {
        super("bad input");
    }
}
