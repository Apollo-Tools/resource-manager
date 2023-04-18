package at.uibk.dps.rm.exception;

public class RuntimeNotSupportedException extends RuntimeException {

    private static final long serialVersionUID = -3831622840655310150L;

    public RuntimeNotSupportedException() {
        super("runtime not supported");
    }
}
