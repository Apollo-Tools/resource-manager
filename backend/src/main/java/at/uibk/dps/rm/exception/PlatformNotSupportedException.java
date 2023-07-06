package at.uibk.dps.rm.exception;

/**
 * The PlatformNotSupportedException indicates that a platform is not supported for a given task.
 *
 * @author matthi-g
 */
public class PlatformNotSupportedException extends RuntimeException {

    private static final long serialVersionUID = 1427293651847202896L;

    /**
     * Create an instance with the message.
     */
    public PlatformNotSupportedException(String message) {
        super(message);
    }
}
