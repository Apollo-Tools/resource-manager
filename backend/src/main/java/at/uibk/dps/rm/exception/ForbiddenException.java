package at.uibk.dps.rm.exception;

import io.vertx.serviceproxy.ServiceException;

/**
 * The ForbiddenException indicates that requested operation is not allowed.
 *
 * @author matthi-g
 */
public class ForbiddenException extends ServiceException {


    private static final long serialVersionUID = 5751839936478857404L;

    /**
     * Create an instance with the message.
     */
    public ForbiddenException(String message) {
        super(409, message);
    }

    /**
     * Create an instance from an existing ForbiddenException.
     */
    public ForbiddenException(ForbiddenException forbiddenException) {
        this(forbiddenException.getMessage());
    }
}
