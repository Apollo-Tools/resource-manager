package at.uibk.dps.rm.exception;

public class NotFoundException extends RuntimeException{

    private static final long serialVersionUID = 3390254567609272315L;

    public NotFoundException() {
        super("not found");
    }
}
