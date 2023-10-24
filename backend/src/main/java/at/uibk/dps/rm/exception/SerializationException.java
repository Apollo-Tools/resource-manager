package at.uibk.dps.rm.exception;

import io.vertx.serviceproxy.ServiceException;

/**
 * The UnauthorizedException indicates that the access to an entity or endpoint was not authorized.
 *
 * @author matthi-g
 */
public class SerializationException extends ServiceException {

    /**
     * Create an instance with the message serialization error.
     */
    public SerializationException() {
        this("the requested operation could not be completed due to a serialization conflict. Please retry the " +
            "operation.");
    }

    /**
     * Create an instance with the message.
     */
    public SerializationException(String message) {
        super(401, message);
    }
}
