package at.uibk.dps.rm.exception;

/**
 * The UsedByOtherEntityException indicates that an entity (to delete) is used by other entities.
 *
 * @author matthi-g
 */
public class UsedByOtherEntityException extends RuntimeException {

    private static final long serialVersionUID = -6217076432249904103L;

    /**
     * Create an instance with the message "entity is used by other entities".
     */
    public UsedByOtherEntityException() {
        super("entity is used by other entities");
    }
}
