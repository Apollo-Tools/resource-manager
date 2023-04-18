package at.uibk.dps.rm.exception;

public class UsedByOtherEntityException extends RuntimeException {

    private static final long serialVersionUID = -6217076432249904103L;

    public UsedByOtherEntityException() {
        super("entity is used by other entities");
    }
}
