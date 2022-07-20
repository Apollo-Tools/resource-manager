package at.uibk.dps.rm.exception;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException() {
        super("already exists");
    }
}
