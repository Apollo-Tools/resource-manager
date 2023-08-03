package at.uibk.dps.rm.exception;

public class MonitoringException extends RuntimeException {

    private static final long serialVersionUID = 569220910175564167L;

    /**
     * Create an instance with the message "unexpected error while monitoring resources".
     */
    public MonitoringException() {
        super("unexpected error while monitoring resources");
    }

    /**
     * Create an instance with the message.
     */
    public MonitoringException(String message) {
        super(message);
    }
}
