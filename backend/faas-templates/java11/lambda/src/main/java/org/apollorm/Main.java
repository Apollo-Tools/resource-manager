package org.apollorm;

/**
 * This class is the main entry point of the function.
 *
 * @author matthi-g
 */
public class Main {
    /**
     * The main function represents the function implementation. Return type and the parameter type
     * are must not be modified. Instead directly modify the classes {@link Result} and
     * {@link Input}
     *
     * @param input the input of the function
     * @return the result of the function
     */
    public static Result main(Input input) {
        int input1 = input.getInput1();
        // Processing

        // Return the result
        Result result = new Result();
        result.setResult(input1);
        return result;
    }
}
