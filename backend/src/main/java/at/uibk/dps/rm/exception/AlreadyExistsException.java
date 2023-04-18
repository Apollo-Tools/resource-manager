package at.uibk.dps.rm.exception;

public class AlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -3122116626800793569L;

    public AlreadyExistsException() {
        super("already exists");
    }
}
