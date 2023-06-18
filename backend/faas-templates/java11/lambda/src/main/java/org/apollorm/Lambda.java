package org.apollorm;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apollorm.jsonhandling.Request;
import org.apollorm.jsonhandling.RequestDeserializer;


import java.io.*;
import java.nio.charset.StandardCharsets;

/************ Boilerplate wrapping code ************/
public class Lambda implements RequestStreamHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Request.class, new RequestDeserializer());
        objectMapper.registerModule(module);
        // Parse input
        byte[] requestBytes = inputStream.readAllBytes();
        String requestBody = new String(requestBytes, StandardCharsets.UTF_8);
        Request request = objectMapper.readValue(requestBody, Request.class);
        // Call function implementation
        Result result = Main.main(request.getInput());
        // Return value
        objectMapper.writeValue(outputStream, result);
    }
}
