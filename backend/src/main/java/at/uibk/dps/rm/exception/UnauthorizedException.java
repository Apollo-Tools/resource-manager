package at.uibk.dps.rm.exception;

public class UnauthorizedException extends RuntimeException {


    private static final long serialVersionUID = 5751839936478857404L;

    public UnauthorizedException() {
        super("unauthorized");
    }
}
