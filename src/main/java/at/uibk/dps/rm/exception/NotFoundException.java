package at.uibk.dps.rm.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException() {
        super("not found");
    }
}
