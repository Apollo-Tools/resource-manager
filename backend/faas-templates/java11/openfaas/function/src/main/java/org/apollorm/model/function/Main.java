package org.apollorm.model.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apollorm.model.FunctionHandler;
import org.apollorm.model.exception.FunctionException;
/**
 * This class is the main entry point of the function.
 *
 * @author matthi-g
 */
public class Main implements FunctionHandler {
    /**
     * Directly modify the classes {@link Result} and {@link Input} or implement custom serialization/deserialization
     * with classes of your choice.
     *
     * @param requestBody the input of the function
     * @return the result of the function
     */
    public String main(String requestBody) throws JsonProcessingException {
        // Parse input
        ObjectMapper objectMapper = new ObjectMapper();
        Input input;
        try {
            input = objectMapper.readValue(requestBody, Input.class);
        } catch (Exception ex) {
            throw new FunctionException("bad input");
        }
        // Processing

        // Return the result
        Result result = new Result();
        result.setResult(input.getInput1());
        return objectMapper.writeValueAsString(result);
    }
}
