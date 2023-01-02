package at.uibk.dps.rm.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("unauthorized");
    }
}
