package at.uibk.dps.rm.exception;

/**
 * The RuntimeNotSupportedException indicates that a demanded runtime is not supported.
 *
 * @author matthi-g
 */
public class RuntimeNotSupportedException extends RuntimeException {

    private static final long serialVersionUID = -3831622840655310150L;

    /**
     * Create an instance with the message "runtime not supported".
     */
    public RuntimeNotSupportedException() {
        super("runtime not supported");
    }
}
