package org.apollorm.model.exception;

public class FunctionException extends RuntimeException {

    private static final long serialVersionUID = -6403666897011374139L;

    /**
     * Create an instance with the message.
     */
    public FunctionException(String message) {
        super(message);
    }
}
