package at.uibk.dps.rm.exception;

import io.vertx.serviceproxy.ServiceException;

public class MonitoringException extends ServiceException {

    private static final long serialVersionUID = 569220910175564167L;

    /**
     * Create an instance with the message "unexpected error while monitoring resources".
     */
    public MonitoringException() {
        this("unexpected error while monitoring resources");
    }


    /**
     * Create an instance with the message.
     */
    public MonitoringException(String message) {
        super(400, message);
    }

    /**
     * Create an instance from an existing MonitoringException.
     */
    public MonitoringException(MonitoringException monitoringException) {
        this(monitoringException.getMessage());
    }
}
