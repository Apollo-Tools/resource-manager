package org.apollorm.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apollorm.model.FunctionHandler;

/**
 * This is a 'Hello World' function which returns the string ``Hello, world``.
 *
 * @author matthi-g
 */
public class Main implements FunctionHandler {
    @Override
    public String main(String requestBody) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Processing
        String res = File.helloWorld();

        // Return the result
        Result result = new Result();
        result.setResult(res);
        return objectMapper.writeValueAsString(result);
    }

    /**
     * This is the entrypoint for local development of the function.
     *
     * @param args command line arguments
     * @throws JsonProcessingException when an error occurs during serialization or deserialization
     */
    public static void main(String[] args) throws JsonProcessingException {
        FunctionHandler handler = new Main();
        String result = handler.main("");
        System.out.println(result);
    }
}
