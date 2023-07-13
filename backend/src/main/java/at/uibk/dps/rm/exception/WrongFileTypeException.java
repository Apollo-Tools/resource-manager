package at.uibk.dps.rm.exception;

/**
 * The WrongFileTypeException indicates that a file has the wrong file type.
 *
 * @author matthi-g
 */
public class WrongFileTypeException extends RuntimeException {
    private static final long serialVersionUID = -8109584371976060903L;

    /**
     * Create an instance with the message "file type is not supported".
     */
    public WrongFileTypeException() {
        super("file type is not supported");
    }
}
