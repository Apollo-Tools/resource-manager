package org.apollorm.model;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The entrypoint class of a function must extend this interface.
 *
 * @author matthi-g
 */
public interface FunctionHandler {

    /**
     * The main function represents the function implementation. Return type and the parameter type
     * must not be modified.
     *
     * @param requestBody the input of the function
     * @return the result of the function
     */
    String main(String requestBody) throws JsonProcessingException;
}
