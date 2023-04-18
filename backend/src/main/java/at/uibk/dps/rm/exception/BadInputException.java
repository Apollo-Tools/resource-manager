package at.uibk.dps.rm.exception;

public class BadInputException extends RuntimeException {

    private static final long serialVersionUID = -6403666897011374139L;

    public BadInputException() {
        super("bad input");
    }
}
