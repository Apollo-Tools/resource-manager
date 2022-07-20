package at.uibk.dps.rm.exception;

public class UsedByOtherEntityException extends RuntimeException {
    public UsedByOtherEntityException() {
        super("entity is used by other entities");
    }
}
