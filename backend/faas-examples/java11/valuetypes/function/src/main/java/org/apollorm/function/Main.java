package org.apollorm.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apollorm.model.FunctionHandler;
import org.apollorm.model.exception.FunctionException;

import java.util.List;

/**
 * This is a 'Hello World' function which returns the string ``Hello, world``.
 *
 * @author matthi-g
 */
public class Main implements FunctionHandler {
    @Override
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
        String result;
        switch (input.getValuetype()) {
            case "array":
                result = objectMapper.writeValueAsString(List.of(1, 2, 3, 4));
                break;
            case "number":
                result = objectMapper.writeValueAsString(5);
                break;
            case "string":
                result = objectMapper.writeValueAsString("value");
                break;
            case "object":
                result = objectMapper.writeValueAsString(input);
                break;
            case "boolean":
            default:
                result = objectMapper.writeValueAsString(false);
        }

        // Return the result
        return result;
    }

    /**
     * This is the entrypoint for local development of the function.
     *
     * @param args command line arguments
     * @throws JsonProcessingException when an error occurs during serialization or deserialization
     */
    public static void main(String[] args) throws JsonProcessingException {
        FunctionHandler handler = new Main();
        String result = handler.main("{\"valuetype\": \"object\"}");
        System.out.println(result);
    }
}
