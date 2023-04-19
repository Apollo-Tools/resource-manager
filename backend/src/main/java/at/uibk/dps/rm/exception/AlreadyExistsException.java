package at.uibk.dps.rm.exception;

/**
 * The AlreadyExistsException indicates that an entity that should be stored already exists
 * or that a unique constraint would be violated with the store of the entity.
 *
 * @author matthi-g
 */
public class AlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -3122116626800793569L;

    /**
     * Create an instance with the message "already exists".
     */
    public AlreadyExistsException() {
        super("already exists");
    }
}
