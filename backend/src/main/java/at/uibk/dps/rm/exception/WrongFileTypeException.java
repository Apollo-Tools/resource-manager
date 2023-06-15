package at.uibk.dps.rm.exception;

public class WrongFileTypeException extends RuntimeException {
    private static final long serialVersionUID = -8109584371976060903L;

    /**
     * Create an instance with the message "file type is not supported".
     */
    public WrongFileTypeException() {
        super("file type is not supported");
    }
}
