package at.uibk.dps.rm.exception;

public class PlatformNotSupportedException extends RuntimeException {

    private static final long serialVersionUID = 1427293651847202896L;

    /**
     * Create an instance with the message.
     */
    public PlatformNotSupportedException(String message) {
        super(message);
    }
}
