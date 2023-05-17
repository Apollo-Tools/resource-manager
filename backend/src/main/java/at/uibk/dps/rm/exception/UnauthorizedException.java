package at.uibk.dps.rm.exception;

/**
 * The UnauthorizedException indicates that the access to an entity or endpoint was not authorized.
 *
 * @author matthi-g
 */
public class UnauthorizedException extends RuntimeException {


    private static final long serialVersionUID = 5751839936478857404L;

    /**
     * Create an instance with the message "unauthorized".
     */
    public UnauthorizedException() {
        super("unauthorized");
    }

    /**
     * Create an instance with the message.
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
