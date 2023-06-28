package at.uibk.dps.rm.exception;

import io.vertx.serviceproxy.ServiceException;

/**
 * The UnauthorizedException indicates that the access to an entity or endpoint was not authorized.
 *
 * @author matthi-g
 */
public class UnauthorizedException extends ServiceException {


    private static final long serialVersionUID = 5751839936478857404L;

    /**
     * Create an instance with the message "unauthorized".
     */
    public UnauthorizedException() {
        this("unauthorized");
    }

    /**
     * Create an instance with the message.
     */
    public UnauthorizedException(String message) {
        super(401, message);
    }

    /**
     * Create an instance from an existing UnauthorizedException.
     */
    public UnauthorizedException(UnauthorizedException unauthorizedException) {
        this(unauthorizedException.getMessage());
    }
}
